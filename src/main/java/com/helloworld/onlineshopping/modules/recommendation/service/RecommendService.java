package com.helloworld.onlineshopping.modules.recommendation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.order.entity.OrderItemEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderItemMapper;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.recommendation.vo.RecommendProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final ProductSpuMapper spuMapper;
    private final OrderItemMapper orderItemMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String HOT_KEY = "hot:products";

    public List<RecommendProductVO> getHotProducts(int limit) {
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(HOT_KEY, 0, limit - 1);
        if (tuples != null && !tuples.isEmpty()) {
            List<RecommendProductVO> result = new ArrayList<>();
            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                Long spuId = Long.valueOf(tuple.getValue().toString());
                ProductSpuEntity spu = spuMapper.selectById(spuId);
                if (spu != null && spu.getStatus() == 1) {
                    result.add(toVO(spu, tuple.getScore(), "Hot selling"));
                }
            }
            if (!result.isEmpty()) return result;
        }
        // Fallback to DB
        List<ProductSpuEntity> list = spuMapper.selectList(
            new LambdaQueryWrapper<ProductSpuEntity>()
                .eq(ProductSpuEntity::getStatus, 1)
                .eq(ProductSpuEntity::getAuditStatus, 1)
                .orderByDesc(ProductSpuEntity::getSalesCount)
                .last("LIMIT " + limit));
        return list.stream().map(s -> toVO(s, null, "Hot selling")).collect(Collectors.toList());
    }

    public List<RecommendProductVO> getSimilarProducts(Long spuId, int limit) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null || spu.getCategoryId() == null) return List.of();
        List<ProductSpuEntity> list = spuMapper.selectList(
            new LambdaQueryWrapper<ProductSpuEntity>()
                .eq(ProductSpuEntity::getStatus, 1)
                .eq(ProductSpuEntity::getAuditStatus, 1)
                .eq(ProductSpuEntity::getCategoryId, spu.getCategoryId())
                .ne(ProductSpuEntity::getId, spuId)
                .orderByDesc(ProductSpuEntity::getSalesCount)
                .last("LIMIT " + limit));
        return list.stream().map(s -> toVO(s, null, "Similar product")).collect(Collectors.toList());
    }

    public List<RecommendProductVO> getPersonalRecommend(int limit) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<OrderItemEntity> bought = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItemEntity>().select(OrderItemEntity::getSpuId));
        if (bought.isEmpty()) return getHotProducts(limit);

        Set<Long> boughtSpuIds = bought.stream().map(OrderItemEntity::getSpuId).collect(Collectors.toSet());
        List<Long> categoryIds = new ArrayList<>();
        for (Long sid : boughtSpuIds) {
            ProductSpuEntity s = spuMapper.selectById(sid);
            if (s != null && s.getCategoryId() != null) categoryIds.add(s.getCategoryId());
        }
        if (categoryIds.isEmpty()) return getHotProducts(limit);

        List<ProductSpuEntity> list = spuMapper.selectList(
            new LambdaQueryWrapper<ProductSpuEntity>()
                .eq(ProductSpuEntity::getStatus, 1)
                .eq(ProductSpuEntity::getAuditStatus, 1)
                .in(ProductSpuEntity::getCategoryId, categoryIds)
                .notIn(ProductSpuEntity::getId, boughtSpuIds)
                .orderByDesc(ProductSpuEntity::getSalesCount)
                .last("LIMIT " + limit));
        return list.stream().map(s -> toVO(s, null, "Based on your purchase history")).collect(Collectors.toList());
    }

    public void refreshHotProducts() {
        List<ProductSpuEntity> all = spuMapper.selectList(
            new LambdaQueryWrapper<ProductSpuEntity>()
                .eq(ProductSpuEntity::getStatus, 1)
                .eq(ProductSpuEntity::getAuditStatus, 1));
        redisTemplate.delete(HOT_KEY);
        for (ProductSpuEntity spu : all) {
            double score = spu.getBrowseCount() * 1.0 + spu.getLikeCount() * 2.0
                + spu.getFavoriteCount() * 3.0 + spu.getSalesCount() * 5.0;
            redisTemplate.opsForZSet().add(HOT_KEY, spu.getId().toString(), score);
        }
        log.info("Refreshed hot products, total: {}", all.size());
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
