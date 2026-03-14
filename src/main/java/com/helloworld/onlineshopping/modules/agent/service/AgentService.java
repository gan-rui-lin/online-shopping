package com.helloworld.onlineshopping.modules.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.agent.entity.AgentTaskEntity;
import com.helloworld.onlineshopping.modules.agent.mapper.AgentTaskMapper;
import com.helloworld.onlineshopping.modules.agent.vo.AgentRecommendationVO;
import com.helloworld.onlineshopping.modules.agent.vo.AgentTaskVO;
import com.helloworld.onlineshopping.modules.cart.entity.CartItemEntity;
import com.helloworld.onlineshopping.modules.cart.mapper.CartItemMapper;
import com.helloworld.onlineshopping.modules.product.entity.ProductSkuEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSkuMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {
    private final AgentTaskMapper taskMapper;
    private final ProductSpuMapper spuMapper;
    private final ProductSkuMapper skuMapper;
    private final CartItemMapper cartItemMapper;
    private final ObjectMapper objectMapper;

    public AgentTaskVO createTask(String userPrompt) {
        Long userId = SecurityUtil.getCurrentUserId();
        AgentTaskEntity task = new AgentTaskEntity();
        task.setUserId(userId);
        task.setTaskType("SHOPPING");
        task.setUserPrompt(userPrompt);
        task.setTaskStatus(0);
        taskMapper.insert(task);

        // Parse budget
        BigDecimal budget = parseBudget(userPrompt);
        // Search products
        LambdaQueryWrapper<ProductSpuEntity> w = new LambdaQueryWrapper<ProductSpuEntity>()
            .eq(ProductSpuEntity::getStatus, 1).eq(ProductSpuEntity::getAuditStatus, 1);
        String keyword = extractKeyword(userPrompt);
        if (keyword != null) w.like(ProductSpuEntity::getTitle, keyword);
        if (budget != null) w.le(ProductSpuEntity::getMinPrice, budget);
        w.orderByDesc(ProductSpuEntity::getSalesCount).last("LIMIT 5");
        List<ProductSpuEntity> spus = spuMapper.selectList(w);

        List<AgentRecommendationVO> recs = new ArrayList<>();
        for (ProductSpuEntity spu : spus) {
            ProductSkuEntity sku = skuMapper.selectOne(new LambdaQueryWrapper<ProductSkuEntity>()
                .eq(ProductSkuEntity::getSpuId, spu.getId()).eq(ProductSkuEntity::getStatus, 1)
                .orderByAsc(ProductSkuEntity::getSalePrice).last("LIMIT 1"));
            if (sku == null) continue;
            AgentRecommendationVO r = new AgentRecommendationVO();
            r.setSpuId(spu.getId());
            r.setSkuId(sku.getId());
            r.setTitle(spu.getTitle());
            r.setMainImage(spu.getMainImage());
            r.setPrice(sku.getSalePrice());
            r.setReason(budget != null ? "Within budget " + budget : "Best seller in category");
            recs.add(r);
        }

        try {
            task.setResultJson(objectMapper.writeValueAsString(recs));
        } catch (Exception e) { task.setResultJson("[]"); }
        task.setTaskStatus(2);
        taskMapper.updateById(task);

        return buildVO(task, recs);
    }

    public AgentTaskVO getTaskResult(Long taskId) {
        Long userId = SecurityUtil.getCurrentUserId();
        AgentTaskEntity task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) throw new BusinessException("Task not found");
        List<AgentRecommendationVO> recs = List.of();
        try { recs = objectMapper.readValue(task.getResultJson(), new TypeReference<>() {}); } catch (Exception ignored) {}
        return buildVO(task, recs);
    }

    public void addToCart(Long taskId, List<Long> skuIds) {
        Long userId = SecurityUtil.getCurrentUserId();
        AgentTaskEntity task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) throw new BusinessException("Task not found");
        for (Long skuId : skuIds) {
            CartItemEntity existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItemEntity>()
                .eq(CartItemEntity::getUserId, userId).eq(CartItemEntity::getSkuId, skuId));
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + 1);
                cartItemMapper.updateById(existing);
            } else {
                CartItemEntity item = new CartItemEntity();
                item.setUserId(userId);
                item.setSkuId(skuId);
                item.setQuantity(1);
                item.setChecked(1);
                cartItemMapper.insert(item);
            }
        }
    }

    private BigDecimal parseBudget(String text) {
        Pattern p = Pattern.compile("(?:under|below|within|budget)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) return new BigDecimal(m.group(1));
        Pattern p2 = Pattern.compile("(\\d+)\\s*(?:yuan|元|dollars|\\$)", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(text);
        if (m2.find()) return new BigDecimal(m2.group(1));
        return null;
    }

    private String extractKeyword(String text) {
        String[] keywords = {"laptop", "phone", "book", "dress", "jacket", "sweater", "airpods", "headphone", "coat", "iphone", "macbook", "samsung"};
        String lower = text.toLowerCase();
        for (String kw : keywords) { if (lower.contains(kw)) return kw; }
        return null;
    }

    private AgentTaskVO buildVO(AgentTaskEntity task, List<AgentRecommendationVO> recs) {
        AgentTaskVO vo = new AgentTaskVO();
        vo.setTaskId(task.getId());
        vo.setTaskType(task.getTaskType());
        vo.setTaskStatus(task.getTaskStatus());
        vo.setUserPrompt(task.getUserPrompt());
        vo.setCreateTime(task.getCreateTime());
        vo.setRecommendations(recs);
        return vo;
    }
}
