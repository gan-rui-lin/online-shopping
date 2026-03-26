package com.helloworld.onlineshopping.modules.recommendation.controller;

import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.recommendation.service.RecommendService;
import com.helloworld.onlineshopping.modules.recommendation.vo.RecommendProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recommend", description = "Recommendation APIs")
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {
    private final RecommendService recommendService;

    @Operation(summary = "Hot products (time-decay scored)")
    @GetMapping("/hot")
    public Result<List<RecommendProductVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(recommendService.getHotProducts(limit));
    }

    @Operation(summary = "Similar products (multi-attribute + co-purchase)")
    @GetMapping("/similar/{spuId}")
    public Result<List<RecommendProductVO>> similar(@PathVariable Long spuId, @RequestParam(defaultValue = "6") int limit) {
        return Result.success(recommendService.getSimilarProducts(spuId, limit));
    }

    @Operation(summary = "Personal recommendations (interest profile + collaborative filtering + hot fill)")
    @GetMapping("/personal")
    public Result<List<RecommendProductVO>> personal(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(recommendService.getPersonalRecommend(limit));
    }
}
