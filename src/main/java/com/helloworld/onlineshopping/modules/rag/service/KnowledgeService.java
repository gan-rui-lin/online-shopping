package com.helloworld.onlineshopping.modules.rag.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.modules.product.entity.ProductSkuEntity;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSkuMapper;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.rag.entity.ProductKnowledgeDocEntity;
import com.helloworld.onlineshopping.modules.rag.mapper.ProductKnowledgeDocMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class KnowledgeService {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{IsHan}]{2,}|[A-Za-z0-9]{2,}");
    private static final Set<String> STOP_WORDS = Set.of(
        "这个", "那个", "哪些", "什么", "怎么", "一下", "是否", "可以", "一下子",
        "what", "which", "with", "this", "that", "then", "from", "have", "will"
    );

    private final ProductKnowledgeDocMapper docMapper;
    private final ProductSpuMapper spuMapper;
    private final ProductSkuMapper skuMapper;

    public void importFromProduct(Long spuId) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) return;

        // Keep this import idempotent and avoid duplicate docs after repeated asks.
        docMapper.delete(new LambdaQueryWrapper<ProductKnowledgeDocEntity>()
            .eq(ProductKnowledgeDocEntity::getSpuId, spuId)
            .in(ProductKnowledgeDocEntity::getSourceType, "PRODUCT_DESC", "SKU_SPEC"));

        // Product overview doc
        ProductKnowledgeDocEntity doc = new ProductKnowledgeDocEntity();
        doc.setSpuId(spuId);
        doc.setTitle("Product Overview");
        doc.setContent(spu.getTitle() + " " + (spu.getSubTitle() != null ? spu.getSubTitle() : "") + " " + (spu.getDetailText() != null ? spu.getDetailText() : ""));
        doc.setSourceType("PRODUCT_DESC");
        doc.setStatus(1);
        docMapper.insert(doc);
        // SKU docs
        List<ProductSkuEntity> skus = skuMapper.selectList(new LambdaQueryWrapper<ProductSkuEntity>().eq(ProductSkuEntity::getSpuId, spuId));
        for (ProductSkuEntity sku : skus) {
            ProductKnowledgeDocEntity skuDoc = new ProductKnowledgeDocEntity();
            skuDoc.setSpuId(spuId);
            skuDoc.setTitle("SKU: " + sku.getSkuName());
            skuDoc.setContent("Spec: " + sku.getSpecJson() + " Price: " + sku.getSalePrice());
            skuDoc.setSourceType("SKU_SPEC");
            skuDoc.setStatus(1);
            docMapper.insert(skuDoc);
        }
    }

    public void ensureKnowledgeImported(Long spuId) {
        Long count = docMapper.selectCount(new LambdaQueryWrapper<ProductKnowledgeDocEntity>()
            .eq(ProductKnowledgeDocEntity::getSpuId, spuId)
            .eq(ProductKnowledgeDocEntity::getStatus, 1));
        if (count == null || count == 0L) {
            importFromProduct(spuId);
        }
    }

    public List<ProductKnowledgeDocEntity> searchRelevant(Long spuId, String question, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        List<ProductKnowledgeDocEntity> docs = docMapper.selectList(new LambdaQueryWrapper<ProductKnowledgeDocEntity>()
            .eq(ProductKnowledgeDocEntity::getSpuId, spuId)
            .eq(ProductKnowledgeDocEntity::getStatus, 1)
            .orderByDesc(ProductKnowledgeDocEntity::getUpdateTime)
            .last("LIMIT 80"));
        if (docs.isEmpty()) {
            return List.of();
        }

        List<String> keywords = extractKeywords(question);
        if (keywords.isEmpty()) {
            return docs.stream().limit(safeLimit).collect(Collectors.toList());
        }

        List<ProductKnowledgeDocEntity> sorted = docs.stream()
            .sorted(Comparator
                .comparingInt((ProductKnowledgeDocEntity doc) -> score(doc, keywords)).reversed()
                .thenComparing(ProductKnowledgeDocEntity::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());

        List<ProductKnowledgeDocEntity> matched = sorted.stream()
            .filter(doc -> score(doc, keywords) > 0)
            .limit(safeLimit)
            .collect(Collectors.toList());

        if (!matched.isEmpty()) {
            return matched;
        }
        return sorted.stream().limit(safeLimit).collect(Collectors.toList());
    }

    public String buildProductSnapshot(Long spuId) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) return "";

        List<String> lines = new ArrayList<>();
        lines.add("Title: " + safe(spu.getTitle()));
        lines.add("SubTitle: " + safe(spu.getSubTitle()));
        lines.add("Brand: " + safe(spu.getBrandName()));
        lines.add("PriceRange: " + safe(spu.getMinPrice()) + " - " + safe(spu.getMaxPrice()));
        lines.add("Detail: " + clip(safe(spu.getDetailText()), 480));

        List<ProductSkuEntity> skus = skuMapper.selectList(
            new LambdaQueryWrapper<ProductSkuEntity>().eq(ProductSkuEntity::getSpuId, spuId));
        if (skus != null && !skus.isEmpty()) {
            int limit = Math.min(3, skus.size());
            for (int i = 0; i < limit; i++) {
                ProductSkuEntity sku = skus.get(i);
                lines.add("SKU: " + safe(sku.getSkuName()) + " | Spec: " + safe(sku.getSpecJson()) + " | Price: " + safe(sku.getSalePrice()));
            }
        }

        return String.join("\n", lines);
    }

    private String safe(Object value) {
        if (value == null) return "";
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? "" : text;
    }

    private String clip(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text == null ? "" : text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private List<String> extractKeywords(String question) {
        if (question == null || question.isBlank()) {
            return List.of();
        }
        String normalized = question.toLowerCase(Locale.ROOT);
        Matcher matcher = TOKEN_PATTERN.matcher(normalized);
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        while (matcher.find()) {
            String token = matcher.group().trim();
            if (token.length() < 2 || STOP_WORDS.contains(token)) {
                continue;
            }
            tokens.add(token);
        }
        return new ArrayList<>(tokens);
    }

    private int score(ProductKnowledgeDocEntity doc, List<String> keywords) {
        String title = safe(doc.getTitle()).toLowerCase(Locale.ROOT);
        String content = safe(doc.getContent()).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String keyword : keywords) {
            if (title.contains(keyword)) {
                score += 4;
            }
            if (content.contains(keyword)) {
                score += 2;
            }
        }
        return score;
    }
}
