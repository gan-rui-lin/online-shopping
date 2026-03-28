package com.helloworld.onlineshopping.modules.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.agent.dto.AgentTaskCreateDTO;
import com.helloworld.onlineshopping.modules.agent.entity.AgentTaskEntity;
import com.helloworld.onlineshopping.modules.agent.mapper.AgentTaskMapper;
import com.helloworld.onlineshopping.modules.agent.vo.AgentRecommendationVO;
import com.helloworld.onlineshopping.modules.agent.vo.AgentTaskVO;
import com.helloworld.onlineshopping.modules.ai.service.AiClient;
import com.helloworld.onlineshopping.modules.cart.entity.CartItemEntity;
import com.helloworld.onlineshopping.modules.cart.mapper.CartItemMapper;
import com.helloworld.onlineshopping.modules.product.entity.ProductSkuEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSkuMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {
    private final AgentTaskMapper taskMapper;
    private final ProductSpuMapper spuMapper;
    private final ProductSkuMapper skuMapper;
    private final CartItemMapper cartItemMapper;
    private final ObjectMapper objectMapper;
    private final AiClient aiClient;

    public AgentTaskVO createTask(AgentTaskCreateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        String taskType = normalizeTaskType(dto.getTaskType());

        AgentTaskEntity task = new AgentTaskEntity();
        task.setUserId(userId);
        task.setTaskType(taskType);
        task.setUserPrompt(buildPromptSummary(dto, taskType));
        task.setTaskStatus(0);
        taskMapper.insert(task);

        List<AgentRecommendationVO> recs = "NECESSITY".equals(taskType)
            ? createNecessityRecommendations(dto)
            : createIntentionRecommendations(dto);

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

    private List<AgentRecommendationVO> createNecessityRecommendations(AgentTaskCreateDTO dto) {
        if (dto.getBindSpuId() == null) {
            throw new BusinessException("Necessity task requires a bound product");
        }
        if (!StringUtils.hasText(dto.getRequiredCategoryName()) && dto.getRequiredCategoryId() == null) {
            throw new BusinessException("Necessity task requires category");
        }

        List<AgentRecommendationVO> recs = new ArrayList<>();
        ProductSpuEntity boundSpu = spuMapper.selectById(dto.getBindSpuId());
        if (boundSpu == null) {
            throw new BusinessException("Bound product not found");
        }
        ProductSkuEntity boundSku = lowestPriceSku(boundSpu.getId());
        if (boundSku != null) {
            AgentRecommendationVO primary = toRecommendation(boundSpu, boundSku,
                "绑定补货商品，建议频次：" + safeFrequency(dto.getFrequency()) + "，每次数量：" + safeQuantity(dto.getQuantity()));
            recs.add(primary);
        }

        LambdaQueryWrapper<ProductSpuEntity> query = new LambdaQueryWrapper<ProductSpuEntity>()
            .eq(ProductSpuEntity::getStatus, 1)
            .eq(ProductSpuEntity::getAuditStatus, 1)
            .ne(ProductSpuEntity::getId, dto.getBindSpuId())
            .orderByDesc(ProductSpuEntity::getSalesCount)
            .last("LIMIT 3");
        if (dto.getRequiredCategoryId() != null) {
            query.eq(ProductSpuEntity::getCategoryId, dto.getRequiredCategoryId());
        }

        for (ProductSpuEntity spu : spuMapper.selectList(query)) {
            ProductSkuEntity sku = lowestPriceSku(spu.getId());
            if (sku == null) {
                continue;
            }
            recs.add(toRecommendation(spu, sku, "同类高销量替代款，适合作为补货备选"));
        }

        return recs;
    }

    private List<AgentRecommendationVO> createIntentionRecommendations(AgentTaskCreateDTO dto) {
        if (!StringUtils.hasText(dto.getIntentRequirement())) {
            throw new BusinessException("Intention task requires requirement description");
        }

        LambdaQueryWrapper<ProductSpuEntity> query = new LambdaQueryWrapper<ProductSpuEntity>()
            .eq(ProductSpuEntity::getStatus, 1)
            .eq(ProductSpuEntity::getAuditStatus, 1)
            .orderByDesc(ProductSpuEntity::getSalesCount)
            .last("LIMIT 30");
        if (dto.getBudgetLimit() != null) {
            query.le(ProductSpuEntity::getMinPrice, dto.getBudgetLimit());
        }
        List<ProductSpuEntity> pool = spuMapper.selectList(query);

        List<String> tokens = tokenizeKeywords(dto.getIntentRequirement() + " " + nullSafe(dto.getPreference()));
        List<ProductSpuEntity> candidates = pool.stream()
            .map(spu -> Map.entry(spu, matchScore(spu, tokens)))
            .filter(entry -> entry.getValue() > 0)
            .sorted((a, b) -> {
                int byScore = Integer.compare(b.getValue(), a.getValue());
                if (byScore != 0) {
                    return byScore;
                }
                Integer aSales = a.getKey().getSalesCount() == null ? 0 : a.getKey().getSalesCount();
                Integer bSales = b.getKey().getSalesCount() == null ? 0 : b.getKey().getSalesCount();
                return Integer.compare(bSales, aSales);
            })
            .limit(6)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            throw new BusinessException("No matching products found, please refine your requirement");
        }

        List<AgentRecommendationVO> recs = new ArrayList<>();
        for (ProductSpuEntity spu : candidates) {
            ProductSkuEntity sku = lowestPriceSku(spu.getId());
            if (sku != null) {
                recs.add(toRecommendation(spu, sku, ""));
            }
        }
        if (recs.isEmpty()) {
            return recs;
        }

        Map<Long, String> aiReasonMap = generateAiReasons(dto, recs);
        recs.forEach(r -> r.setReason(aiReasonMap.getOrDefault(r.getSpuId(), "符合你的需求与偏好，且性价比较高")));
        return recs;
    }

    private List<String> tokenizeKeywords(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        String normalized = text
            .replace('，', ' ')
            .replace('。', ' ')
            .replace('、', ' ')
            .replace(',', ' ')
            .replace('.', ' ')
            .replace('/', ' ')
            .replace('|', ' ')
            .trim();
        return Arrays.stream(normalized.split("\\s+"))
            .map(String::trim)
            .filter(s -> s.length() >= 2)
            .limit(8)
            .collect(Collectors.toList());
    }

    private int matchScore(ProductSpuEntity spu, List<String> tokens) {
        if (tokens.isEmpty()) {
            return 0;
        }
        String haystack = (nullSafe(spu.getTitle()) + " " + nullSafe(spu.getSubTitle()) + " " + nullSafe(spu.getDetailText())).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String token : tokens) {
            String t = token.toLowerCase(Locale.ROOT);
            if (haystack.contains(t)) {
                score += 2;
            }
            if (nullSafe(spu.getTitle()).toLowerCase(Locale.ROOT).contains(t)) {
                score += 2;
            }
        }
        return score;
    }

    private Map<Long, String> generateAiReasons(AgentTaskCreateDTO dto, List<AgentRecommendationVO> recs) {
        try {
            String candidateText = recs.stream()
                .map(r -> "spuId=" + r.getSpuId() + ", title=" + r.getTitle() + ", price=" + r.getPrice())
                .collect(Collectors.joining("\n"));
            String systemPrompt = "你是电商导购助手。根据用户需求对候选商品生成推荐理由。" +
                "仅返回 JSON 数组，格式为 [{\"spuId\":123,\"reason\":\"推荐理由\"}]。";
            String userPrompt = "用户需求：" + dto.getIntentRequirement() + "\n偏好：" + nullSafe(dto.getPreference()) +
                "\n预算上限：" + (dto.getBudgetLimit() == null ? "不限" : dto.getBudgetLimit()) + "\n候选商品：\n" + candidateText;
            String aiText = aiClient.chat(systemPrompt, userPrompt);
            JsonNode root = objectMapper.readTree(aiText);
            if (!root.isArray()) {
                return Map.of();
            }

            Map<Long, String> reasonMap = new HashMap<>();
            for (JsonNode node : root) {
                Long spuId = node.path("spuId").asLong(0L);
                String reason = node.path("reason").asText("");
                if (spuId > 0 && StringUtils.hasText(reason)) {
                    reasonMap.put(spuId, reason);
                }
            }
            return reasonMap;
        } catch (Exception ex) {
            log.warn("Generate AI reasons failed, fallback to default reason", ex);
            return Map.of();
        }
    }

    private ProductSkuEntity lowestPriceSku(Long spuId) {
        return skuMapper.selectOne(new LambdaQueryWrapper<ProductSkuEntity>()
            .eq(ProductSkuEntity::getSpuId, spuId)
            .eq(ProductSkuEntity::getStatus, 1)
            .orderByAsc(ProductSkuEntity::getSalePrice)
            .last("LIMIT 1"));
    }

    private AgentRecommendationVO toRecommendation(ProductSpuEntity spu, ProductSkuEntity sku, String reason) {
        AgentRecommendationVO r = new AgentRecommendationVO();
        r.setSpuId(spu.getId());
        r.setSkuId(sku.getId());
        r.setTitle(spu.getTitle());
        r.setMainImage(spu.getMainImage());
        r.setPrice(sku.getSalePrice());
        r.setReason(reason);
        return r;
    }

    private String normalizeTaskType(String taskType) {
        String normalized = taskType == null ? "" : taskType.trim().toUpperCase(Locale.ROOT);
        if (!"NECESSITY".equals(normalized) && !"INTENTION".equals(normalized)) {
            throw new BusinessException("Unsupported task type");
        }
        return normalized;
    }

    private String buildPromptSummary(AgentTaskCreateDTO dto, String taskType) {
        if ("NECESSITY".equals(taskType)) {
            return "必需品代购|类目=" + nullSafe(dto.getRequiredCategoryName()) +
                "|类目ID=" + (dto.getRequiredCategoryId() == null ? "" : dto.getRequiredCategoryId()) +
                "|频次=" + safeFrequency(dto.getFrequency()) +
                "|绑定商品=" + (dto.getBindSpuId() == null ? "" : dto.getBindSpuId()) +
                "|数量=" + safeQuantity(dto.getQuantity());
        }
        return "意向代购|需求=" + nullSafe(dto.getIntentRequirement()) +
            "|偏好=" + nullSafe(dto.getPreference()) +
            "|预算=" + (dto.getBudgetLimit() == null ? "不限" : dto.getBudgetLimit());
    }

    private String safeFrequency(String frequency) {
        return StringUtils.hasText(frequency) ? frequency : "每月一次";
    }

    private int safeQuantity(Integer quantity) {
        return quantity == null || quantity < 1 ? 1 : quantity;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
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
