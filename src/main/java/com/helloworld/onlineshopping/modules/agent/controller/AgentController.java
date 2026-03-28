package com.helloworld.onlineshopping.modules.agent.controller;

import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.agent.dto.AgentAddToCartDTO;
import com.helloworld.onlineshopping.modules.agent.dto.AgentTaskCreateDTO;
import com.helloworld.onlineshopping.modules.agent.service.AgentService;
import com.helloworld.onlineshopping.modules.agent.vo.AgentTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Agent", description = "Agent Shopping APIs")
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {
    private final AgentService agentService;

    @Operation(summary = "Create agent task")
    @PostMapping("/task")
    public Result<AgentTaskVO> create(@Valid @RequestBody AgentTaskCreateDTO dto) {
        return Result.success(agentService.createTask(dto));
    }

    @Operation(summary = "Get task result")
    @GetMapping("/task/{taskId}")
    public Result<AgentTaskVO> get(@PathVariable Long taskId) {
        return Result.success(agentService.getTaskResult(taskId));
    }

    @Operation(summary = "Add recommendations to cart")
    @PostMapping("/task/{taskId}/add-to-cart")
    public Result<Void> addToCart(@PathVariable Long taskId, @Valid @RequestBody AgentAddToCartDTO dto) {
        agentService.addToCart(taskId, dto.getSkuIds());
        return Result.success();
    }
}
