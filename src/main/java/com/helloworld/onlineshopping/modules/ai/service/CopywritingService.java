package com.helloworld.onlineshopping.modules.ai.service;

import com.helloworld.onlineshopping.common.exception.BusinessException;
import com.helloworld.onlineshopping.modules.ai.vo.CopywritingResultVO;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CopywritingService {

    private final ProductSpuMapper spuMapper;
    private final AiClient aiClient;

    public CopywritingResultVO generateTitle(Long spuId) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) throw new BusinessException("Product not found");
        String prompt = "Generate 3 creative product title variants for this product.";
        String result = aiClient.chat(prompt, spu.getTitle() + " " + (spu.getSubTitle() != null ? spu.getSubTitle() : ""));
        CopywritingResultVO vo = new CopywritingResultVO();
        vo.setContent(result);
        vo.setVariants(Arrays.asList(result.split("\n")));
        return vo;
    }

    public CopywritingResultVO generateDescription(String keywords, String targetAudience, String style) {
        String prompt = "Generate marketing copy. Target: " + targetAudience + ". Style: " + style;
        String result = aiClient.chat(prompt, keywords);
        CopywritingResultVO vo = new CopywritingResultVO();
        vo.setContent(result);
        vo.setVariants(List.of(result));
        return vo;
    }

    public CopywritingResultVO generateSellingPoints(Long spuId) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu == null) throw new BusinessException("Product not found");
        String prompt = "Extract and enhance 5 key selling points for this product.";
        String result = aiClient.chat(prompt, spu.getTitle() + " " + (spu.getDetailText() != null ? spu.getDetailText() : ""));
        CopywritingResultVO vo = new CopywritingResultVO();
        vo.setContent(result);
        vo.setVariants(Arrays.asList(result.split("\n")));
        return vo;
    }
}
