package com.helloworld.onlineshopping.modules.recommendation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.modules.order.entity.OrderItemEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Lightweight item-based collaborative filtering via co-purchase analysis.
 * "Users who bought X also bought Y" implemented without ML infrastructure.
 *
 * Strategy: group order_items by order_no, then for each pair of items in
 * the same order, increment a co-purchase counter. The result is stored
 * in Redis sorted sets for fast retrieval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoPurchaseService {

    private final OrderItemMapper orderItemMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String COPURCHASE_KEY_PREFIX = "copurchase:";
    private static final int COPURCHASE_TTL_HOURS = 12;
    private static final int MAX_RELATED = 20;

    /**
     * Get products frequently co-purchased with the given spuId.
     * Returns a list of spuIds sorted by co-purchase frequency.
     */
    public List<Long> getRelatedProducts(Long spuId, int limit) {
        String key = COPURCHASE_KEY_PREFIX + spuId;
        try {
            Set<Object> members = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
            if (members != null && !members.isEmpty()) {
                return members.stream()
                    .map(m -> Long.valueOf(m.toString()))
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.debug("Co-purchase cache miss for spuId={}: {}", spuId, e.getMessage());
        }
        return List.of();
    }

    /**
     * Get co-purchased products for a set of spuIds the user has bought,
     * excluding items already purchased. Returns merged + ranked results.
     */
    public List<Long> getRecommendationsForUser(Set<Long> purchasedSpuIds, int limit) {
        Map<Long, Double> scores = new HashMap<>();
        for (Long spuId : purchasedSpuIds) {
            List<Long> related = getRelatedProducts(spuId, MAX_RELATED);
            for (int i = 0; i < related.size(); i++) {
                Long relatedId = related.get(i);
                if (!purchasedSpuIds.contains(relatedId)) {
                    // Weight by position: earlier = stronger co-purchase signal
                    double positionWeight = 1.0 / (1 + i * 0.1);
                    scores.merge(relatedId, positionWeight, Double::sum);
                }
            }
        }

        return scores.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Rebuild the entire co-purchase matrix from order data.
     * Called by a nightly scheduler.
     */
    public void rebuildCoPurchaseMatrix() {
        log.info("Starting co-purchase matrix rebuild...");
        long start = System.currentTimeMillis();

        List<OrderItemEntity> allItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemEntity>()
                .select(OrderItemEntity::getOrderNo, OrderItemEntity::getSpuId));

        // Group by orderNo
        Map<String, List<Long>> orderSpuMap = new HashMap<>();
        for (OrderItemEntity item : allItems) {
            orderSpuMap.computeIfAbsent(item.getOrderNo(), k -> new ArrayList<>())
                .add(item.getSpuId());
        }

        // Build co-purchase counts: for each order with multiple distinct items,
        // increment the pair counter
        Map<Long, Map<Long, Integer>> coPurchaseCounts = new HashMap<>();
        for (List<Long> spuIds : orderSpuMap.values()) {
            Set<Long> distinct = new HashSet<>(spuIds);
            if (distinct.size() < 2) continue;

            List<Long> distinctList = new ArrayList<>(distinct);
            for (int i = 0; i < distinctList.size(); i++) {
                for (int j = 0; j < distinctList.size(); j++) {
                    if (i == j) continue;
                    Long a = distinctList.get(i);
                    Long b = distinctList.get(j);
                    coPurchaseCounts.computeIfAbsent(a, k -> new HashMap<>())
                        .merge(b, 1, Integer::sum);
                }
            }
        }

        // Write to Redis sorted sets
        int count = 0;
        for (Map.Entry<Long, Map<Long, Integer>> entry : coPurchaseCounts.entrySet()) {
            String key = COPURCHASE_KEY_PREFIX + entry.getKey();
            redisTemplate.delete(key);
            for (Map.Entry<Long, Integer> pair : entry.getValue().entrySet()) {
                redisTemplate.opsForZSet().add(key, pair.getKey().toString(), pair.getValue());
            }
            redisTemplate.expire(key, COPURCHASE_TTL_HOURS, TimeUnit.HOURS);
            count++;
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("Co-purchase matrix rebuild complete: {} products indexed in {}ms", count, elapsed);
    }
}
