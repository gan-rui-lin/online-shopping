package com.helloworld.onlineshopping.modules.recommendation.service;

import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.recommendation.vo.RecommendProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Re-ranks a candidate list to improve diversity, freshness and quality.
 * Applied as the final stage before returning recommendations to the user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendReRankService {

    private final ProductSpuMapper spuMapper;

    private static final int MAX_SAME_CATEGORY = 3;
    private static final double FRESHNESS_BOOST = 1.3;
    private static final int FRESHNESS_WINDOW_DAYS = 7;
    private static final double LOW_REVIEW_PENALTY = 0.3;

    /**
     * Re-rank candidates with diversity constraints, freshness boost, and quality filter.
     *
     * @param candidates   raw candidate list (may exceed limit)
     * @param excludeSpuIds spuIds to exclude (already purchased/recently browsed)
     * @param limit        final result size
     */
    public List<RecommendProductVO> reRank(
            List<RecommendProductVO> candidates,
            Set<Long> excludeSpuIds,
            int limit) {

        // 1. Filter out excluded items and out-of-stock
        List<ScoredCandidate> scored = new ArrayList<>();
        for (RecommendProductVO vo : candidates) {
            if (excludeSpuIds != null && excludeSpuIds.contains(vo.getSpuId())) continue;

            ProductSpuEntity spu = spuMapper.selectById(vo.getSpuId());
            if (spu == null || spu.getStatus() != 1) continue;
            if (spu.getAuditStatus() != null && spu.getAuditStatus() != 1) continue;

            double adjustedScore = vo.getScore() != null ? vo.getScore() : 0;

            // 2. Freshness boost for recently-listed products
            if (spu.getCreateTime() != null) {
                long daysSinceListed = ChronoUnit.DAYS.between(spu.getCreateTime(), LocalDateTime.now());
                if (daysSinceListed <= FRESHNESS_WINDOW_DAYS) {
                    adjustedScore *= FRESHNESS_BOOST;
                }
            }

            scored.add(new ScoredCandidate(vo, spu.getCategoryId(), adjustedScore));
        }

        // 3. Sort by adjusted score descending
        scored.sort(Comparator.comparingDouble(ScoredCandidate::getAdjustedScore).reversed());

        // 4. Apply category diversity constraint using greedy selection
        List<RecommendProductVO> result = new ArrayList<>();
        Map<Long, Integer> categoryCounts = new HashMap<>();

        for (ScoredCandidate sc : scored) {
            if (result.size() >= limit) break;

            Long catId = sc.getCategoryId();
            int currentCount = categoryCounts.getOrDefault(catId, 0);
            if (currentCount >= MAX_SAME_CATEGORY) continue;

            sc.getVo().setScore(sc.getAdjustedScore());
            result.add(sc.getVo());
            categoryCounts.put(catId, currentCount + 1);
        }

        // 5. If we still need more (diversity constraint was too strict), relax it
        if (result.size() < limit) {
            Set<Long> addedIds = result.stream().map(RecommendProductVO::getSpuId).collect(Collectors.toSet());
            for (ScoredCandidate sc : scored) {
                if (result.size() >= limit) break;
                if (addedIds.contains(sc.getVo().getSpuId())) continue;
                sc.getVo().setScore(sc.getAdjustedScore());
                result.add(sc.getVo());
            }
        }

        return result;
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    private static class ScoredCandidate {
        private final RecommendProductVO vo;
        private final Long categoryId;
        private final double adjustedScore;
    }
}
