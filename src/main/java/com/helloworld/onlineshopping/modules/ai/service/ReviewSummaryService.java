package com.helloworld.onlineshopping.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.modules.ai.vo.ReviewSummaryVO;
import com.helloworld.onlineshopping.modules.review.entity.ProductReviewEntity;
import com.helloworld.onlineshopping.modules.review.mapper.ProductReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewSummaryService {

    private final ProductReviewMapper reviewMapper;
    private final AiClient aiClient;

    public ReviewSummaryVO summarizeReviews(Long spuId, String locale) {
        List<ProductReviewEntity> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<ProductReviewEntity>()
                .eq(ProductReviewEntity::getSpuId, spuId)
                .eq(ProductReviewEntity::getReviewStatus, 1));

        ReviewSummaryVO vo = new ReviewSummaryVO();
        vo.setSpuId(spuId);
        vo.setTotalReviews(reviews.size());

        if (reviews.isEmpty()) {
            vo.setAverageScore(0.0);
            vo.setPros(List.of());
            vo.setCons(List.of());
            vo.setSummary(isEnglish(locale) ? "No reviews yet." : "暂无评论。");
            return vo;
        }

        double avg = reviews.stream().mapToInt(ProductReviewEntity::getScore).average().orElse(0);
        vo.setAverageScore(Math.round(avg * 10.0) / 10.0);

        String reviewTexts = reviews.stream()
            .filter(r -> r.getContent() != null)
            .map(r -> "Score:" + r.getScore() + " " + r.getContent())
            .collect(Collectors.joining("\n"));

        String systemPrompt;
        String userPrompt = "Reviews:\n" + reviewTexts;
        if (isEnglish(locale)) {
            systemPrompt = "Summarize product reviews. Output exactly in this format:\n" +
                "PROS: item1, item2, item3\n" +
                "CONS: item1, item2\n" +
                "SUMMARY: one short paragraph in English.";
        } else {
            systemPrompt = "请总结商品评论，输出固定格式：\n" +
                "PROS: 优点1, 优点2, 优点3\n" +
                "CONS: 不足1, 不足2\n" +
                "SUMMARY: 一段简短中文总结。";
        }
        String result = aiClient.chat(systemPrompt, userPrompt);

        // Parse mock response
        vo.setPros(extractSection(result, "PROS:"));
        vo.setCons(extractSection(result, "CONS:"));

        String summary = result.contains("SUMMARY:")
            ? result.substring(result.indexOf("SUMMARY:") + 8).trim()
            : result;
        vo.setSummary(summary);
        return vo;
    }

    private List<String> extractSection(String text, String marker) {
        if (!text.contains(marker)) return List.of();
        String section = text.substring(text.indexOf(marker) + marker.length());
        if (section.contains("\n")) section = section.substring(0, section.indexOf("\n"));
        return Arrays.stream(section.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    private boolean isEnglish(String locale) {
        return locale != null && locale.toLowerCase(Locale.ROOT).startsWith("en");
    }
}
