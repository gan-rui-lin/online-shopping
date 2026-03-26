package com.helloworld.onlineshopping.modules.recommendation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.order.entity.OrderEntity;
import com.helloworld.onlineshopping.modules.order.entity.OrderItemEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderItemMapper;
import com.helloworld.onlineshopping.modules.order.mapper.OrderMapper;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.recommendation.service.UserInterestProfileService.InterestProfile;
import com.helloworld.onlineshopping.modules.recommendation.vo.RecommendProductVO;
import com.helloworld.onlineshopping.modules.review.entity.ProductReviewEntity;
import com.helloworld.onlineshopping.modules.review.mapper.ProductReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Multi-strategy recommendation engine combining:
 * - Popularity ranking with time-decay (hot products)
 * - Content-based similarity (multi-attribute matching)
 * - User interest profiles (browse + favorite + purchase + review signals)
 * - Collaborative filtering (co-purchase analysis)
 * - Re-ranking for diversity, freshness and quality
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final ProductSpuMapper spuMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductReviewMapper reviewMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserInterestProfileService interestProfileService;
    private final CoPurchaseService coPurchaseService;
    private final RecommendReRankService reRankService;

    private static final String HOT_KEY = "hot:products";

    // ======================== HOT PRODUCTS ========================

    @Cacheable(value = "product:hot", key = "#limit")
    public List<RecommendProductVO> getHotProducts(int limit) {
        // Try Redis ZSET first (populated by refreshHotProducts scheduler)
        Set<ZSetOperations.TypedTuple<Object>> tuples =
            redisTemplate.opsForZSet().reverseRangeWithScores(HOT_KEY, 0, limit * 2L - 1);

        List<RecommendProductVO> candidates = new ArrayList<>();
        if (tuples != null && !tuples.isEmpty()) {
            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                Long spuId = Long.valueOf(tuple.getValue().toString());
                ProductSpuEntity spu = spuMapper.selectById(spuId);
                if (spu != null && spu.getStatus() == 1) {
                    candidates.add(toVO(spu, tuple.getScore(), "Hot selling"));
                }
            }
        }

        // Fallback to DB if Redis is empty
        if (candidates.isEmpty()) {
            List<ProductSpuEntity> list = spuMapper.selectList(
                new LambdaQueryWrapper<ProductSpuEntity>()
                    .eq(ProductSpuEntity::getStatus, 1)
                    .eq(ProductSpuEntity::getAuditStatus, 1)
                    .orderByDesc(ProductSpuEntity::getSalesCount)
                    .last("LIMIT " + limit * 2));
            candidates = list.stream()
                .map(s -> toVO(s, (double) s.getSalesCount(), "Hot selling"))
                .collect(Collectors.toList());
        }

        // Apply re-ranking for diversity
        return reRankService.reRank(candidates, Set.of(), limit);
    }

    // ======================== SIMILAR PRODUCTS ========================

    public List<RecommendProductVO> getSimilarProducts(Long spuId, int limit) {
        ProductSpuEntity sourceSpu = spuMapper.selectById(spuId);
        if (sourceSpu == null) return List.of();

        List<RecommendProductVO> candidates = new ArrayList<>();

        // Strategy A: Same category products
        if (sourceSpu.getCategoryId() != null) {
            List<ProductSpuEntity> categoryMatches = spuMapper.selectList(
                new LambdaQueryWrapper<ProductSpuEntity>()
                    .eq(ProductSpuEntity::getStatus, 1)
                    .eq(ProductSpuEntity::getAuditStatus, 1)
                    .eq(ProductSpuEntity::getCategoryId, sourceSpu.getCategoryId())
                    .ne(ProductSpuEntity::getId, spuId)
                    .orderByDesc(ProductSpuEntity::getSalesCount)
                    .last("LIMIT " + limit * 3));

            for (ProductSpuEntity spu : categoryMatches) {
                double score = computeSimilarityScore(sourceSpu, spu);
                candidates.add(toVO(spu, score, "Similar product"));
            }
        }

        // Strategy B: Same brand across categories
        if (sourceSpu.getBrandName() != null && !sourceSpu.getBrandName().isBlank()) {
            List<ProductSpuEntity> brandMatches = spuMapper.selectList(
                new LambdaQueryWrapper<ProductSpuEntity>()
                    .eq(ProductSpuEntity::getStatus, 1)
                    .eq(ProductSpuEntity::getAuditStatus, 1)
                    .eq(ProductSpuEntity::getBrandName, sourceSpu.getBrandName())
                    .ne(ProductSpuEntity::getId, spuId)
                    .ne(ProductSpuEntity::getCategoryId, sourceSpu.getCategoryId())
                    .orderByDesc(ProductSpuEntity::getSalesCount)
                    .last("LIMIT " + limit));

            for (ProductSpuEntity spu : brandMatches) {
                double score = computeSimilarityScore(sourceSpu, spu) * 0.8;
                candidates.add(toVO(spu, score, "Same brand"));
            }
        }

        // Strategy C: Co-purchased items
        List<Long> coPurchased = coPurchaseService.getRelatedProducts(spuId, limit);
        for (Long relatedId : coPurchased) {
            ProductSpuEntity spu = spuMapper.selectById(relatedId);
            if (spu != null && spu.getStatus() == 1) {
                candidates.add(toVO(spu, 50.0, "Frequently bought together"));
            }
        }

        // Deduplicate by spuId, keeping the highest-scored entry
        Map<Long, RecommendProductVO> deduped = new LinkedHashMap<>();
        for (RecommendProductVO vo : candidates) {
            deduped.merge(vo.getSpuId(), vo, (existing, incoming) ->
                (incoming.getScore() != null && (existing.getScore() == null || incoming.getScore() > existing.getScore()))
                    ? incoming : existing);
        }

        return reRankService.reRank(new ArrayList<>(deduped.values()), Set.of(spuId), limit);
    }

    // ======================== PERSONAL RECOMMENDATIONS ========================

    public List<RecommendProductVO> getPersonalRecommend(int limit) {
        Long userId;
        try {
            userId = SecurityUtil.getCurrentUserId();
        } catch (Exception e) {
            return getHotProducts(limit);
        }

        InterestProfile profile = interestProfileService.getProfile(userId);

        // Collect candidates from multiple strategies
        int candidatePool = limit * 4;
        List<RecommendProductVO> candidates = new ArrayList<>();

        // Strategy 1: Interest-profile-based (category + brand preferences)
        candidates.addAll(getInterestBasedCandidates(profile, candidatePool));

        // Strategy 2: Co-purchase collaborative filtering
        if (!profile.getPurchasedSpuIds().isEmpty()) {
            List<Long> cfCandidates = coPurchaseService.getRecommendationsForUser(
                profile.getPurchasedSpuIds(), candidatePool);
            for (Long spuId : cfCandidates) {
                ProductSpuEntity spu = spuMapper.selectById(spuId);
                if (spu != null && spu.getStatus() == 1) {
                    candidates.add(toVO(spu, 60.0, "Customers also bought"));
                }
            }
        }

        // Strategy 3: Fill with hot products if candidates are sparse
        if (candidates.size() < limit) {
            List<RecommendProductVO> hotFill = getHotProductCandidates(candidatePool);
            for (RecommendProductVO vo : hotFill) {
                vo.setScore(vo.getScore() != null ? vo.getScore() * 0.5 : 10.0);
                vo.setReason("Trending now");
                candidates.add(vo);
            }
        }

        // Deduplicate
        Map<Long, RecommendProductVO> deduped = new LinkedHashMap<>();
        for (RecommendProductVO vo : candidates) {
            deduped.merge(vo.getSpuId(), vo, (existing, incoming) ->
                (incoming.getScore() != null && (existing.getScore() == null || incoming.getScore() > existing.getScore()))
                    ? incoming : existing);
        }

        // Re-rank with exclusion of already-purchased items
        return reRankService.reRank(
            new ArrayList<>(deduped.values()),
            profile.getPurchasedSpuIds(),
            limit);
    }

    // ======================== HOT PRODUCT REFRESH (SCHEDULER) ========================

    /**
     * Rebuild the hot products Redis ZSET with time-decay scoring.
     * Score formula: (engagement + velocity + reviewBonus) * recencyDecay
     */
    public void refreshHotProducts() {
        List<ProductSpuEntity> all = spuMapper.selectList(
            new LambdaQueryWrapper<ProductSpuEntity>()
                .eq(ProductSpuEntity::getStatus, 1)
                .eq(ProductSpuEntity::getAuditStatus, 1));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);

        redisTemplate.delete(HOT_KEY);
        for (ProductSpuEntity spu : all) {
            double engagementScore =
                spu.getBrowseCount() * 1.0
                + spu.getLikeCount() * 2.0
                + spu.getFavoriteCount() * 3.0
                + spu.getSalesCount() * 5.0;

            // Velocity: recent 7-day sales
            double recentSales = countRecentSales(spu.getId(), weekAgo);
            double velocityScore = recentSales * 10.0;

            // Review quality bonus
            double reviewBonus = computeReviewBonus(spu.getId(), spu.getSalesCount());

            // Time decay based on product creation date
            double recencyDecay = 1.0;
            if (spu.getCreateTime() != null) {
                long daysSinceCreated = ChronoUnit.DAYS.between(spu.getCreateTime(), now);
                recencyDecay = Math.exp(-0.005 * daysSinceCreated);
                recencyDecay = Math.max(recencyDecay, 0.1);
            }

            double finalScore = (engagementScore + velocityScore + reviewBonus) * recencyDecay;
            redisTemplate.opsForZSet().add(HOT_KEY, spu.getId().toString(), finalScore);
        }
        log.info("Refreshed hot products with time-decay scoring, total: {}", all.size());
    }

    // ======================== PRIVATE HELPERS ========================

    private List<RecommendProductVO> getInterestBasedCandidates(InterestProfile profile, int limit) {
        List<RecommendProductVO> result = new ArrayList<>();

        if (profile.getCategoryScores().isEmpty()) return result;

        // Get top-N preferred categories sorted by score
        List<Map.Entry<Long, Double>> topCategories = profile.getCategoryScores().entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());

        for (Map.Entry<Long, Double> catEntry : topCategories) {
            Long categoryId = catEntry.getKey();
            double categoryWeight = catEntry.getValue();

            List<ProductSpuEntity> spus = spuMapper.selectList(
                new LambdaQueryWrapper<ProductSpuEntity>()
                    .eq(ProductSpuEntity::getStatus, 1)
                    .eq(ProductSpuEntity::getAuditStatus, 1)
                    .eq(ProductSpuEntity::getCategoryId, categoryId)
                    .notIn(!profile.getPurchasedSpuIds().isEmpty(),
                        ProductSpuEntity::getId, profile.getPurchasedSpuIds())
                    .orderByDesc(ProductSpuEntity::getSalesCount)
                    .last("LIMIT " + (limit / 3 + 1)));

            for (ProductSpuEntity spu : spus) {
                double score = categoryWeight;

                // Boost if brand also matches preference
                if (spu.getBrandName() != null) {
                    Double brandScore = profile.getBrandScores().get(spu.getBrandName());
                    if (brandScore != null && brandScore > 0) {
                        score += brandScore * 0.5;
                    }
                }

                String reason = categoryWeight > 20 ? "Based on your interests"
                    : categoryWeight > 5 ? "You might like this" : "Recommended for you";
                result.add(toVO(spu, score, reason));
            }
        }

        return result;
    }

    private List<RecommendProductVO> getHotProductCandidates(int limit) {
        Set<ZSetOperations.TypedTuple<Object>> tuples =
            redisTemplate.opsForZSet().reverseRangeWithScores(HOT_KEY, 0, limit - 1L);

        List<RecommendProductVO> result = new ArrayList<>();
        if (tuples == null) return result;

        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            Long spuId = Long.valueOf(tuple.getValue().toString());
            ProductSpuEntity spu = spuMapper.selectById(spuId);
            if (spu != null && spu.getStatus() == 1) {
                result.add(toVO(spu, tuple.getScore(), "Hot selling"));
            }
        }
        return result;
    }

    /**
     * Multi-attribute similarity score between source and candidate product.
     * Factors: category match, price proximity, brand match, sales popularity.
     */
    private double computeSimilarityScore(ProductSpuEntity source, ProductSpuEntity candidate) {
        double score = 0;

        // Category match (strongest signal): 40 points
        if (Objects.equals(source.getCategoryId(), candidate.getCategoryId())) {
            score += 40;
        }

        // Price proximity: up to 20 points (closer price = higher score)
        if (source.getMinPrice() != null && candidate.getMinPrice() != null
            && source.getMinPrice().compareTo(BigDecimal.ZERO) > 0) {
            double priceDiff = Math.abs(source.getMinPrice().doubleValue() - candidate.getMinPrice().doubleValue());
            double priceRatio = priceDiff / source.getMinPrice().doubleValue();
            score += Math.max(0, 20 * (1 - priceRatio));
        }

        // Brand match: 15 points
        if (source.getBrandName() != null && source.getBrandName().equals(candidate.getBrandName())) {
            score += 15;
        }

        // Sales popularity: up to 15 points (log-scaled)
        if (candidate.getSalesCount() != null && candidate.getSalesCount() > 0) {
            score += Math.min(15, Math.log1p(candidate.getSalesCount()) * 3);
        }

        // Engagement: up to 10 points
        int engagement = (candidate.getBrowseCount() != null ? candidate.getBrowseCount() : 0)
            + (candidate.getFavoriteCount() != null ? candidate.getFavoriteCount() * 2 : 0);
        score += Math.min(10, Math.log1p(engagement) * 2);

        return score;
    }

    private double countRecentSales(Long spuId, LocalDateTime since) {
        Long count = orderItemMapper.selectCount(
            new LambdaQueryWrapper<OrderItemEntity>()
                .eq(OrderItemEntity::getSpuId, spuId)
                .ge(OrderItemEntity::getCreateTime, since));
        return count != null ? count : 0;
    }

    private double computeReviewBonus(Long spuId, Integer salesCount) {
        List<ProductReviewEntity> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<ProductReviewEntity>()
                .eq(ProductReviewEntity::getSpuId, spuId)
                .select(ProductReviewEntity::getScore));

        if (reviews.isEmpty()) return 0;

        double avgScore = reviews.stream()
            .mapToInt(ProductReviewEntity::getScore)
            .average()
            .orElse(3.0);

        // Bonus only for well-reviewed products (avg > 4.0)
        if (avgScore >= 4.0) {
            return (salesCount != null ? salesCount : 0) * 2.0;
        }
        return 0;
    }

    private RecommendProductVO toVO(ProductSpuEntity spu, Double score, String reason) {
        RecommendProductVO vo = new RecommendProductVO();
        vo.setSpuId(spu.getId());
        vo.setTitle(spu.getTitle());
        vo.setMainImage(spu.getMainImage());
        vo.setMinPrice(spu.getMinPrice());
        vo.setSalesCount(spu.getSalesCount());
        vo.setScore(score);
        vo.setReason(reason);
        return vo;
    }
}
