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
import java.util.List;
@Service
@RequiredArgsConstructor
public class KnowledgeService {
    private final ProductKnowledgeDocMapper docMapper;
    private final ProductSpuMapper spuMapper;
    private final ProductSkuMapper skuMapper;

    public void importFromProduct(Long spuId) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) return;
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

    public List<ProductKnowledgeDocEntity> searchRelevant(Long spuId, String question, int limit) {
        String[] keywords = question.split("\\s+");
        LambdaQueryWrapper<ProductKnowledgeDocEntity> w = new LambdaQueryWrapper<ProductKnowledgeDocEntity>()
            .eq(ProductKnowledgeDocEntity::getSpuId, spuId).eq(ProductKnowledgeDocEntity::getStatus, 1);
        if (keywords.length > 0) {
            w.and(q -> { for (String kw : keywords) { q.or().like(ProductKnowledgeDocEntity::getContent, kw); } });
        }
        w.last("LIMIT " + limit);
        return docMapper.selectList(w);
    }
}
