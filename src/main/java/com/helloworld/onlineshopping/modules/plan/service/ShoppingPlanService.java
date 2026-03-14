package com.helloworld.onlineshopping.modules.plan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.cart.entity.CartItemEntity;
import com.helloworld.onlineshopping.modules.cart.mapper.CartItemMapper;
import com.helloworld.onlineshopping.modules.plan.dto.ShoppingPlanCreateDTO;
import com.helloworld.onlineshopping.modules.plan.dto.ShoppingPlanItemDTO;
import com.helloworld.onlineshopping.modules.plan.entity.ShoppingPlanEntity;
import com.helloworld.onlineshopping.modules.plan.entity.ShoppingPlanItemEntity;
import com.helloworld.onlineshopping.modules.plan.mapper.ShoppingPlanItemMapper;
import com.helloworld.onlineshopping.modules.plan.mapper.ShoppingPlanMapper;
import com.helloworld.onlineshopping.modules.plan.vo.ShoppingPlanItemVO;
import com.helloworld.onlineshopping.modules.plan.vo.ShoppingPlanVO;
import com.helloworld.onlineshopping.modules.product.entity.ProductSkuEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSkuMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingPlanService {
    private final ShoppingPlanMapper planMapper;
    private final ShoppingPlanItemMapper itemMapper;
    private final ProductSpuMapper spuMapper;
    private final ProductSkuMapper skuMapper;
    private final CartItemMapper cartItemMapper;

    @Transactional
    public void createPlan(ShoppingPlanCreateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        ShoppingPlanEntity plan = new ShoppingPlanEntity();
        plan.setUserId(userId);
        plan.setPlanName(dto.getPlanName());
        plan.setTriggerTime(dto.getTriggerTime());
        plan.setPlanStatus(0);
        plan.setBudgetAmount(dto.getBudgetAmount());
        plan.setRemark(dto.getRemark());
        planMapper.insert(plan);
        if (dto.getItems() != null) {
            for (ShoppingPlanItemDTO i : dto.getItems()) {
                ShoppingPlanItemEntity item = new ShoppingPlanItemEntity();
                item.setPlanId(plan.getId());
                item.setKeyword(i.getKeyword());
                item.setCategoryId(i.getCategoryId());
                item.setExpectedPriceMin(i.getExpectedPriceMin());
                item.setExpectedPriceMax(i.getExpectedPriceMax());
                item.setQuantity(i.getQuantity() != null ? i.getQuantity() : 1);
                itemMapper.insert(item);
            }
        }
    }

    public List<ShoppingPlanVO> getPlanList() {
        Long userId = SecurityUtil.getCurrentUserId();
        return planMapper.selectList(new LambdaQueryWrapper<ShoppingPlanEntity>()
            .eq(ShoppingPlanEntity::getUserId, userId).orderByDesc(ShoppingPlanEntity::getCreateTime))
            .stream().map(this::buildVO).collect(Collectors.toList());
    }

    public ShoppingPlanVO getPlanDetail(Long planId) {
        ShoppingPlanEntity plan = planMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(SecurityUtil.getCurrentUserId()))
            throw new BusinessException("Plan not found");
        return buildVO(plan);
    }

    public void cancelPlan(Long planId) {
        ShoppingPlanEntity plan = planMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(SecurityUtil.getCurrentUserId()))
            throw new BusinessException("Plan not found");
        if (plan.getPlanStatus() > 1)
            throw new BusinessException("Cannot cancel this plan");
        plan.setPlanStatus(3);
        planMapper.updateById(plan);
    }

    @Transactional
    public void executePlan(Long planId) {
        Long userId = SecurityUtil.getCurrentUserId();
        ShoppingPlanEntity plan = planMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId))
            throw new BusinessException("Plan not found");
        List<ShoppingPlanItemEntity> items = itemMapper.selectList(
            new LambdaQueryWrapper<ShoppingPlanItemEntity>().eq(ShoppingPlanItemEntity::getPlanId, planId));
        for (ShoppingPlanItemEntity item : items) {
            LambdaQueryWrapper<ProductSpuEntity> w = new LambdaQueryWrapper<ProductSpuEntity>()
                .eq(ProductSpuEntity::getStatus, 1).eq(ProductSpuEntity::getAuditStatus, 1);
            if (StringUtils.hasText(item.getKeyword()))
                w.like(ProductSpuEntity::getTitle, item.getKeyword());
            if (item.getCategoryId() != null)
                w.eq(ProductSpuEntity::getCategoryId, item.getCategoryId());
            if (item.getExpectedPriceMin() != null)
                w.ge(ProductSpuEntity::getMinPrice, item.getExpectedPriceMin());
            if (item.getExpectedPriceMax() != null)
                w.le(ProductSpuEntity::getMinPrice, item.getExpectedPriceMax());
            w.orderByDesc(ProductSpuEntity::getSalesCount).last("LIMIT 1");
            ProductSpuEntity matched = spuMapper.selectOne(w);
            if (matched != null) {
                item.setMatchedSpuId(matched.getId());
                itemMapper.updateById(item);
                ProductSkuEntity sku = skuMapper.selectOne(new LambdaQueryWrapper<ProductSkuEntity>()
                    .eq(ProductSkuEntity::getSpuId, matched.getId()).eq(ProductSkuEntity::getStatus, 1)
                    .orderByAsc(ProductSkuEntity::getSalePrice).last("LIMIT 1"));
                if (sku != null) {
                    CartItemEntity existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItemEntity>()
                        .eq(CartItemEntity::getUserId, userId).eq(CartItemEntity::getSkuId, sku.getId()));
                    if (existing != null) {
                        existing.setQuantity(existing.getQuantity() + item.getQuantity());
                        cartItemMapper.updateById(existing);
                    } else {
                        CartItemEntity ci = new CartItemEntity();
                        ci.setUserId(userId);
                        ci.setSkuId(sku.getId());
                        ci.setQuantity(item.getQuantity());
                        ci.setChecked(1);
                        cartItemMapper.insert(ci);
                    }
                }
            }
        }
        plan.setPlanStatus(2);
        planMapper.updateById(plan);
    }

    private ShoppingPlanVO buildVO(ShoppingPlanEntity plan) {
        ShoppingPlanVO vo = new ShoppingPlanVO();
        vo.setPlanId(plan.getId());
        vo.setPlanName(plan.getPlanName());
        vo.setTriggerTime(plan.getTriggerTime());
        vo.setPlanStatus(plan.getPlanStatus());
        vo.setBudgetAmount(plan.getBudgetAmount());
        vo.setRemark(plan.getRemark());
        vo.setCreateTime(plan.getCreateTime());
        List<ShoppingPlanItemEntity> items = itemMapper.selectList(
            new LambdaQueryWrapper<ShoppingPlanItemEntity>().eq(ShoppingPlanItemEntity::getPlanId, plan.getId()));
        vo.setItems(items.stream().map(i -> {
            ShoppingPlanItemVO iv = new ShoppingPlanItemVO();
            iv.setId(i.getId());
            iv.setKeyword(i.getKeyword());
            iv.setCategoryId(i.getCategoryId());
            iv.setExpectedPriceMin(i.getExpectedPriceMin());
            iv.setExpectedPriceMax(i.getExpectedPriceMax());
            iv.setQuantity(i.getQuantity());
            iv.setMatchedSpuId(i.getMatchedSpuId());
            if (i.getMatchedSpuId() != null) {
                ProductSpuEntity s = spuMapper.selectById(i.getMatchedSpuId());
                if (s != null) iv.setMatchedProductTitle(s.getTitle());
            }
            return iv;
        }).collect(Collectors.toList()));
        return vo;
    }
}
