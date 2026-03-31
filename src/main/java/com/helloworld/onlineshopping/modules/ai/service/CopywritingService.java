package com.helloworld.onlineshopping.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.modules.ai.vo.CopywritingResultVO;
import com.helloworld.onlineshopping.modules.ai.vo.ProductEvaluationVO;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.review.entity.ProductReviewEntity;
import com.helloworld.onlineshopping.modules.review.mapper.ProductReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CopywritingService {

    private final ProductSpuMapper spuMapper;
    private final ProductReviewMapper reviewMapper;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public CopywritingResultVO generateTitle(Long spuId, String locale) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) throw new BusinessException("Product not found");
        String prompt = isEnglish(locale)
            ? "Generate 3 creative product title variants in English."
            : "生成 3 个中文商品标题变体。";
        String result = aiClient.chat(prompt, spu.getTitle() + " " + (spu.getSubTitle() != null ? spu.getSubTitle() : ""));
        CopywritingResultVO vo = new CopywritingResultVO();
        vo.setContent(result);
        vo.setVariants(Arrays.asList(result.split("\n")));
        return vo;
    }

    public CopywritingResultVO generateDescription(String keywords, String targetAudience, String style, String locale) {
        String prompt = isEnglish(locale)
            ? "Generate marketing copy in English. Target: " + safe(targetAudience) + ". Style: " + safe(style)
            : "生成中文营销文案。目标人群：" + safe(targetAudience) + "。风格：" + safe(style);
        String result = aiClient.chat(prompt, keywords);
        CopywritingResultVO vo = new CopywritingResultVO();
        vo.setContent(result);
        vo.setVariants(List.of(result));
        return vo;
    }

    public CopywritingResultVO generateSellingPoints(Long spuId, String locale) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) throw new BusinessException("Product not found");
        String prompt = isEnglish(locale)
            ? "Extract and enhance 5 key selling points. Output as English Markdown bullet list."
            : "提炼并强化 5 条卖点，使用中文 Markdown 列表输出。";
        String result = aiClient.chat(prompt, spu.getTitle() + " " + (spu.getDetailText() != null ? spu.getDetailText() : ""));
        CopywritingResultVO vo = new CopywritingResultVO();
        vo.setContent(result);
        vo.setVariants(Arrays.asList(result.split("\n")));
        return vo;
    }

    public ProductEvaluationVO evaluateProduct(Long spuId, String locale) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("Product not found");
        }

        List<ProductReviewEntity> reviews = reviewMapper.selectList(new LambdaQueryWrapper<ProductReviewEntity>()
            .eq(ProductReviewEntity::getSpuId, spuId)
            .eq(ProductReviewEntity::getReviewStatus, 1)
            .last("LIMIT 20"));

        String reviewText = reviews.stream()
            .map(r -> "评分=" + r.getScore() + ", 内容=" + (r.getContent() == null ? "" : r.getContent()))
            .collect(Collectors.joining("\n"));

        String systemPrompt;
        String userPrompt;
        if (isEnglish(locale)) {
            systemPrompt = "You are an e-commerce product analyst. Generate a multi-dimensional evaluation." +
                "Return JSON only: " +
                "{\"overallLevel\":\"High/Medium/Low\",\"qualityScore\":0-10,\"valueScore\":0-10," +
                "\"scenarioFit\":\"Target scenarios\",\"potentialRisks\":[\"Risk1\",\"Risk2\"],\"summary\":\"One-line summary\"}";
            userPrompt = "Title=" + spu.getTitle() +
                "\nSubtitle=" + safe(spu.getSubTitle()) +
                "\nBrand=" + safe(spu.getBrandName()) +
                "\nMin price=" + safe(spu.getMinPrice()) +
                "\nSales=" + safe(spu.getSalesCount()) +
                "\nReview count=" + reviews.size() +
                "\nReview samples=" + reviewText;
        } else {
            systemPrompt = "你是电商商品分析助手。请综合商品基本信息、销量与评价，生成多维度评价。" +
                "必须输出 JSON，格式：" +
                "{\"overallLevel\":\"高/中/低\",\"qualityScore\":0-10,\"valueScore\":0-10," +
                "\"scenarioFit\":\"适合人群与场景\",\"potentialRisks\":[\"风险1\",\"风险2\"],\"summary\":\"一句话总结\"}";
            userPrompt = "商品标题=" + spu.getTitle() +
                "\n副标题=" + safe(spu.getSubTitle()) +
                "\n品牌=" + safe(spu.getBrandName()) +
                "\n最低价=" + safe(spu.getMinPrice()) +
                "\n销量=" + safe(spu.getSalesCount()) +
                "\n评价条数=" + reviews.size() +
                "\n评价样本=" + reviewText;
        }
        String aiText = aiClient.chat(systemPrompt, userPrompt);

        ProductEvaluationVO vo = new ProductEvaluationVO();
        vo.setSpuId(spuId);
        if (isEnglish(locale)) {
            vo.setOverallLevel("Medium");
            vo.setQualityScore(7.0);
            vo.setValueScore(7.0);
            vo.setScenarioFit("General daily scenarios");
            vo.setPotentialRisks(List.of());
            vo.setSummary("Balanced performance; choose based on budget and preferences.");
        } else {
            vo.setOverallLevel("中");
            vo.setQualityScore(7.0);
            vo.setValueScore(7.0);
            vo.setScenarioFit("通用日常场景");
            vo.setPotentialRisks(List.of());
            vo.setSummary("综合表现均衡，可按预算与偏好选择。");
        }

        try {
            JsonNode root = objectMapper.readTree(aiText);
            if (root.isObject()) {
                String overall = root.path("overallLevel").asText();
                if (StringUtils.hasText(overall)) {
                    vo.setOverallLevel(overall);
                }
                vo.setQualityScore(root.path("qualityScore").asDouble(vo.getQualityScore()));
                vo.setValueScore(root.path("valueScore").asDouble(vo.getValueScore()));

                String fit = root.path("scenarioFit").asText();
                if (StringUtils.hasText(fit)) {
                    vo.setScenarioFit(fit);
                }
                if (root.path("potentialRisks").isArray()) {
                    vo.setPotentialRisks(objectMapper.convertValue(root.path("potentialRisks"), List.class));
                }
                String summary = root.path("summary").asText();
                if (StringUtils.hasText(summary)) {
                    vo.setSummary(summary);
                }
            }
        } catch (Exception ignore) {
            // Fallback to default fields if AI response is not strict JSON.
        }

        return vo;
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private boolean isEnglish(String locale) {
        return locale != null && locale.toLowerCase(java.util.Locale.ROOT).startsWith("en");
    }
}
