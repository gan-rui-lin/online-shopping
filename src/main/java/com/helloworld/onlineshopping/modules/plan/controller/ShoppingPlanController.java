package com.helloworld.onlineshopping.modules.plan.controller;

import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.plan.dto.ShoppingPlanCreateDTO;
import com.helloworld.onlineshopping.modules.plan.service.ShoppingPlanService;
import com.helloworld.onlineshopping.modules.plan.vo.ShoppingPlanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Plan", description = "Shopping Plan APIs")
@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class ShoppingPlanController {
    private final ShoppingPlanService planService;

    @Operation(summary = "Create plan")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ShoppingPlanCreateDTO dto) {
        planService.createPlan(dto);
        return Result.success();
    }

    @Operation(summary = "List plans")
    @GetMapping("/list")
    public Result<List<ShoppingPlanVO>> list() {
        return Result.success(planService.getPlanList());
    }

    @Operation(summary = "Plan detail")
    @GetMapping("/{planId}")
    public Result<ShoppingPlanVO> detail(@PathVariable Long planId) {
        return Result.success(planService.getPlanDetail(planId));
    }

    @Operation(summary = "Cancel plan")
    @PostMapping("/{planId}/cancel")
    public Result<Void> cancel(@PathVariable Long planId) {
        planService.cancelPlan(planId);
        return Result.success();
    }

    @Operation(summary = "Execute plan")
    @PostMapping("/{planId}/execute")
    public Result<Void> execute(@PathVariable Long planId) {
        planService.executePlan(planId);
        return Result.success();
    }
}
