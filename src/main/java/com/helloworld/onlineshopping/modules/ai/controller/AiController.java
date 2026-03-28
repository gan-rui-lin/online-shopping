package com.helloworld.onlineshopping.modules.ai.controller;

import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.ai.dto.CopywritingRequestDTO;
import com.helloworld.onlineshopping.modules.ai.service.CopywritingService;
import com.helloworld.onlineshopping.modules.ai.service.ReviewSummaryService;
import com.helloworld.onlineshopping.modules.ai.vo.CopywritingResultVO;
import com.helloworld.onlineshopping.modules.ai.vo.ProductEvaluationVO;
import com.helloworld.onlineshopping.modules.ai.vo.ReviewSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI", description = "AI Copywriting & Review Summary APIs")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final CopywritingService copywritingService;
    private final ReviewSummaryService reviewSummaryService;

    @Operation(summary = "Generate title")
    @PostMapping("/copywriting/title/{spuId}")
    public Result<CopywritingResultVO> title(@PathVariable Long spuId) {
        return Result.success(copywritingService.generateTitle(spuId));
    }

    @Operation(summary = "Generate description")
    @PostMapping("/copywriting/description")
    public Result<CopywritingResultVO> description(@RequestBody CopywritingRequestDTO dto) {
        return Result.success(copywritingService.generateDescription(dto.getKeywords(), dto.getTargetAudience(), dto.getStyle()));
    }

    @Operation(summary = "Generate selling points")
    @PostMapping("/copywriting/selling-points/{spuId}")
    public Result<CopywritingResultVO> sellingPoints(@PathVariable Long spuId) {
        return Result.success(copywritingService.generateSellingPoints(spuId));
    }

    @Operation(summary = "Review summary")
    @GetMapping("/review-summary/{spuId}")
    public Result<ReviewSummaryVO> reviewSummary(@PathVariable Long spuId) {
        return Result.success(reviewSummaryService.summarizeReviews(spuId));
    }

    @Operation(summary = "Product multi-dimensional evaluation")
    @GetMapping("/copywriting/evaluate/{spuId}")
    public Result<ProductEvaluationVO> evaluate(@PathVariable Long spuId) {
        return Result.success(copywritingService.evaluateProduct(spuId));
    }
}
