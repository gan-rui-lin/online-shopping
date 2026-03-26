package com.helloworld.onlineshopping.modules.recommendation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.modules.behavior.entity.UserBrowseHistoryEntity;
import com.helloworld.onlineshopping.modules.behavior.mapper.UserBrowseHistoryMapper;
import com.helloworld.onlineshopping.modules.favorite.entity.UserFavoriteEntity;
import com.helloworld.onlineshopping.modules.favorite.mapper.UserFavoriteMapper;
import com.helloworld.onlineshopping.modules.order.entity.OrderEntity;
import com.helloworld.onlineshopping.modules.order.entity.OrderItemEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderItemMapper;
import com.helloworld.onlineshopping.modules.order.mapper.OrderMapper;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.review.entity.ProductReviewEntity;
import com.helloworld.onlineshopping.modules.review.mapper.ProductReviewMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Builds a weighted user interest profile from multiple behavioral signals.
 * The profile aggregates category preferences with time-decay weighting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInterestProfileService {

    private final UserBrowseHistoryMapper browseMapper;
    private final UserFavoriteMapper favoriteMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductSpuMapper spuMapper;
    private final ProductReviewMapper reviewMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PROFILE_KEY_PREFIX = "user:interest:";
    private static final int PROFILE_TTL_MINUTES = 30;

    // Signal weights
    private static final double WEIGHT_BROWSE = 1.0;
    private static final double WEIGHT_FAVORITE = 5.0;
    private static final double WEIGHT_PURCHASE = 10.0;
    private static final double WEIGHT_POSITIVE_REVIEW = 8.0;
    private static final double WEIGHT_NEGATIVE_REVIEW = -5.0;

    // Time-decay half-lives in days
    private static final double BROWSE_HALF_LIFE = 7.0;
    private static final double FAVORITE_HALF_LIFE = 30.0;
    private static final double PURCHASE_HALF_LIFE = 60.0;

    @Data
    public static class InterestProfile {
        /** categoryId -> aggregated interest score */
        private Map<Long, Double> categoryScores = new HashMap<>();
        /** brandName -> aggregated interest score */
        private Map<String, Double> brandScores = new HashMap<>();
        /** spuIds the user has already interacted with (for exclusion) */
        private Set<Long> interactedSpuIds = new HashSet<>();
        /** spuIds the user has purchased (stronger exclusion) */
        private Set<Long> purchasedSpuIds = new HashSet<>();
    }

    /**
     * Build or retrieve cached interest profile for a user.
     */
    public InterestProfile getProfile(Long userId) {
        String cacheKey = PROFILE_KEY_PREFIX + userId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof InterestProfile) {
                return (InterestProfile) cached;
            }
        } catch (Exception e) {
            log.debug("Cache miss for interest profile: {}", e.getMessage());
        }

        InterestProfile profile = buildProfile(userId);

        try {
            redisTemplate.opsForValue().set(cacheKey, profile, PROFILE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to cache interest profile: {}", e.getMessage());
        }
        return profile;
    }

    /**
     * Invalidate a user's cached interest profile (called after significant events).
     */
    public void invalidateProfile(Long userId) {
        try {
            redisTemplate.delete(PROFILE_KEY_PREFIX + userId);
        } catch (Exception ignored) {}
    }

    private InterestProfile buildProfile(Long userId) {
        InterestProfile profile = new InterestProfile();
        LocalDateTime now = LocalDateTime.now();

        accumulateBrowseSignals(userId, now, profile);
        accumulateFavoriteSignals(userId, now, profile);
        accumulatePurchaseSignals(userId, now, profile);
        accumulateReviewSignals(userId, profile);

        return profile;
    }

    private void accumulateBrowseSignals(Long userId, LocalDateTime now, InterestProfile profile) {
        LocalDateTime cutoff = now.minusDays(30);
        List<UserBrowseHistoryEntity> history = browseMapper.selectList(
            new LambdaQueryWrapper<UserBrowseHistoryEntity>()
                .eq(UserBrowseHistoryEntity::getUserId, userId)
                .ge(UserBrowseHistoryEntity::getBrowseTime, cutoff)
                .orderByDesc(UserBrowseHistoryEntity::getBrowseTime)
                .last("LIMIT 200"));

        for (UserBrowseHistoryEntity h : history) {
            profile.getInteractedSpuIds().add(h.getSpuId());
            double decay = timeDecay(h.getBrowseTime(), now, BROWSE_HALF_LIFE);
            addSpuSignal(h.getSpuId(), WEIGHT_BROWSE * decay, profile);
        }
    }

    private void accumulateFavoriteSignals(Long userId, LocalDateTime now, InterestProfile profile) {
        List<UserFavoriteEntity> favorites = favoriteMapper.selectList(
            new LambdaQueryWrapper<UserFavoriteEntity>()
                .eq(UserFavoriteEntity::getUserId, userId));

        for (UserFavoriteEntity f : favorites) {
            profile.getInteractedSpuIds().add(f.getSpuId());
            double decay = timeDecay(f.getCreateTime(), now, FAVORITE_HALF_LIFE);
            addSpuSignal(f.getSpuId(), WEIGHT_FAVORITE * decay, profile);
        }
    }

    private void accumulatePurchaseSignals(Long userId, LocalDateTime now, InterestProfile profile) {
        List<OrderEntity> orders = orderMapper.selectList(
            new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getUserId, userId)
                .in(OrderEntity::getOrderStatus, List.of(1, 2, 3))
                .select(OrderEntity::getOrderNo, OrderEntity::getCreateTime));

        if (orders.isEmpty()) return;

        Map<String, LocalDateTime> orderTimeMap = orders.stream()
            .collect(Collectors.toMap(OrderEntity::getOrderNo, OrderEntity::getCreateTime, (a, b) -> a));

        List<OrderItemEntity> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemEntity>()
                .in(OrderItemEntity::getOrderNo, orderTimeMap.keySet())
                .select(OrderItemEntity::getSpuId, OrderItemEntity::getOrderNo));

        for (OrderItemEntity item : items) {
            profile.getInteractedSpuIds().add(item.getSpuId());
            profile.getPurchasedSpuIds().add(item.getSpuId());
            LocalDateTime orderTime = orderTimeMap.getOrDefault(item.getOrderNo(), now);
            double decay = timeDecay(orderTime, now, PURCHASE_HALF_LIFE);
            addSpuSignal(item.getSpuId(), WEIGHT_PURCHASE * decay, profile);
        }
    }

    private void accumulateReviewSignals(Long userId, InterestProfile profile) {
        List<ProductReviewEntity> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<ProductReviewEntity>()
                .eq(ProductReviewEntity::getUserId, userId)
                .select(ProductReviewEntity::getSpuId, ProductReviewEntity::getScore));

        for (ProductReviewEntity r : reviews) {
            double weight = r.getScore() >= 4 ? WEIGHT_POSITIVE_REVIEW
                : r.getScore() <= 2 ? WEIGHT_NEGATIVE_REVIEW : 0;
            if (weight != 0) {
                addSpuSignal(r.getSpuId(), weight, profile);
            }
        }
    }

    private void addSpuSignal(Long spuId, double weight, InterestProfile profile) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) return;

        if (spu.getCategoryId() != null) {
            profile.getCategoryScores().merge(spu.getCategoryId(), weight, Double::sum);
        }
        if (spu.getBrandName() != null && !spu.getBrandName().isBlank()) {
            profile.getBrandScores().merge(spu.getBrandName(), weight, Double::sum);
        }
    }

    /**
     * Exponential time decay: score = e^(-lambda * days), where half-life determines lambda.
     */
    private double timeDecay(LocalDateTime eventTime, LocalDateTime now, double halfLifeDays) {
        if (eventTime == null) return 0.5;
        long daysBetween = ChronoUnit.DAYS.between(eventTime, now);
        if (daysBetween < 0) daysBetween = 0;
        double lambda = Math.log(2) / halfLifeDays;
        return Math.exp(-lambda * daysBetween);
    }
}
