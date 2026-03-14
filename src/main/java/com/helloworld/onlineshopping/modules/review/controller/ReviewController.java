package com.helloworld.onlineshopping.modules.review.controller;

import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.review.dto.ReviewCreateDTO;
import com.helloworld.onlineshopping.modules.review.dto.ReviewQueryDTO;
import com.helloworld.onlineshopping.modules.review.dto.ReviewReplyDTO;
import com.helloworld.onlineshopping.modules.review.service.ReviewService;
import com.helloworld.onlineshopping.modules.review.vo.ReviewStatisticVO;
import com.helloworld.onlineshopping.modules.review.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review", description = "Product Review APIs")
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Create review")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ReviewCreateDTO dto) {
        reviewService.createReview(dto);
        return Result.success();
    }

    @Operation(summary = "Get product reviews")
    @GetMapping("/product/{spuId}")
    public Result<PageResult<ReviewVO>> productReviews(@PathVariable Long spuId, ReviewQueryDTO dto) {
        dto.setSpuId(spuId);
        return Result.success(reviewService.getProductReviews(dto));
    }

    @Operation(summary = "Get review statistics")
    @GetMapping("/product/{spuId}/statistics")
    public Result<ReviewStatisticVO> statistics(@PathVariable Long spuId) {
        return Result.success(reviewService.getReviewStatistics(spuId));
    }

    @Operation(summary = "Reply to review (merchant)")
    @PostMapping("/{reviewId}/reply")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<Void> reply(@PathVariable Long reviewId, @Valid @RequestBody ReviewReplyDTO dto) {
        reviewService.replyToReview(reviewId, dto);
        return Result.success();
    }
}
