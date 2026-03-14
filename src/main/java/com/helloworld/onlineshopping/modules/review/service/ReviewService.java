package com.helloworld.onlineshopping.modules.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.order.entity.OrderEntity;
import com.helloworld.onlineshopping.modules.order.entity.OrderItemEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderItemMapper;
import com.helloworld.onlineshopping.modules.order.mapper.OrderMapper;
import com.helloworld.onlineshopping.modules.review.dto.ReviewCreateDTO;
import com.helloworld.onlineshopping.modules.review.dto.ReviewQueryDTO;
import com.helloworld.onlineshopping.modules.review.entity.ProductReviewEntity;
import com.helloworld.onlineshopping.modules.review.mapper.ProductReviewMapper;
import com.helloworld.onlineshopping.modules.review.vo.ReviewStatisticVO;
import com.helloworld.onlineshopping.modules.review.vo.ReviewVO;
import com.helloworld.onlineshopping.modules.user.entity.UserEntity;
import com.helloworld.onlineshopping.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ProductReviewMapper reviewMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Transactional
    public void createReview(ReviewCreateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        // Validate order item
        OrderItemEntity orderItem = orderItemMapper.selectById(dto.getOrderItemId());
        if (orderItem == null) {
            throw new BusinessException("Order item not found");
        }

        // Validate order belongs to user and is completed
        OrderEntity order = orderMapper.selectOne(
            new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderNo, orderItem.getOrderNo()));
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("No permission to review this item");
        }
        if (order.getOrderStatus() != 3) {
            throw new BusinessException("Can only review completed orders");
        }

        // Check if already reviewed
        if (orderItem.getReviewStatus() == 1) {
            throw new BusinessException("Already reviewed");
        }

        // Create review
        ProductReviewEntity review = new ProductReviewEntity();
        review.setOrderItemId(dto.getOrderItemId());
        review.setOrderNo(orderItem.getOrderNo());
        review.setUserId(userId);
        review.setSpuId(orderItem.getSpuId());
        review.setSkuId(orderItem.getSkuId());
        review.setScore(dto.getScore());
        review.setContent(dto.getContent());
        review.setImageUrls(dto.getImageUrls() != null ? String.join(",", dto.getImageUrls()) : null);
        review.setAnonymousFlag(dto.getAnonymousFlag());
        review.setReviewStatus(1);
        reviewMapper.insert(review);

        // Update order item review status
        orderItem.setReviewStatus(1);
        orderItemMapper.updateById(orderItem);
    }

    public PageResult<ReviewVO> getProductReviews(ReviewQueryDTO dto) {
        Page<ProductReviewEntity> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<ProductReviewEntity> wrapper = new LambdaQueryWrapper<ProductReviewEntity>()
            .eq(ProductReviewEntity::getSpuId, dto.getSpuId())
            .eq(ProductReviewEntity::getReviewStatus, 1)
            .orderByDesc(ProductReviewEntity::getCreateTime);

        if (dto.getScore() != null) {
            wrapper.eq(ProductReviewEntity::getScore, dto.getScore());
        }

        Page<ProductReviewEntity> result = reviewMapper.selectPage(page, wrapper);
        List<ReviewVO> voList = result.getRecords().stream().map(review -> {
            ReviewVO vo = new ReviewVO();
            vo.setReviewId(review.getId());
            vo.setUserId(review.getUserId());
            vo.setAnonymousFlag(review.getAnonymousFlag());
            vo.setScore(review.getScore());
            vo.setContent(review.getContent());
            vo.setReplyContent(review.getReplyContent());
            vo.setReplyTime(review.getReplyTime());
            vo.setCreateTime(review.getCreateTime());

            if (review.getImageUrls() != null && !review.getImageUrls().isEmpty()) {
                vo.setImageUrls(Arrays.asList(review.getImageUrls().split(",")));
            }

            // Get user info (anonymize if needed)
            if (review.getAnonymousFlag() == 1) {
                vo.setNickname("Anonymous");
                vo.setAvatarUrl(null);
            } else {
                UserEntity user = userMapper.selectById(review.getUserId());
                if (user != null) {
                    vo.setNickname(user.getNickname());
                    vo.setAvatarUrl(user.getAvatarUrl());
                }
            }

            // Get SKU name from order item
            OrderItemEntity oi = orderItemMapper.selectById(review.getOrderItemId());
            if (oi != null) {
                vo.setSkuName(oi.getSkuName());
            }

            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    public ReviewStatisticVO getReviewStatistics(Long spuId) {
        List<ProductReviewEntity> all = reviewMapper.selectList(
            new LambdaQueryWrapper<ProductReviewEntity>()
                .eq(ProductReviewEntity::getSpuId, spuId)
                .eq(ProductReviewEntity::getReviewStatus, 1));

        ReviewStatisticVO vo = new ReviewStatisticVO();
        vo.setSpuId(spuId);
        vo.setTotalCount(all.size());
        vo.setGoodCount((int) all.stream().filter(r -> r.getScore() >= 4).count());
        vo.setMediumCount((int) all.stream().filter(r -> r.getScore() == 3).count());
        vo.setBadCount((int) all.stream().filter(r -> r.getScore() <= 2).count());
        if (!all.isEmpty()) {
            vo.setGoodRate(new BigDecimal(vo.getGoodCount())
                .divide(new BigDecimal(vo.getTotalCount()), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100)));
        } else {
            vo.setGoodRate(BigDecimal.ZERO);
        }
        return vo;
    }
}
