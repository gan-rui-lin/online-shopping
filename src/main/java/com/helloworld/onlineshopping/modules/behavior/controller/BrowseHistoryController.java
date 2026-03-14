package com.helloworld.onlineshopping.modules.behavior.controller;

import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.behavior.service.BrowseHistoryService;
import com.helloworld.onlineshopping.modules.behavior.vo.BrowseHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Behavior", description = "User Behavior APIs")
@RestController
@RequestMapping("/api/behavior/history")
@RequiredArgsConstructor
public class BrowseHistoryController {
    private final BrowseHistoryService browseHistoryService;

    @Operation(summary = "Get browse history")
    @GetMapping
    public Result<PageResult<BrowseHistoryVO>> history(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(browseHistoryService.getBrowseHistory(pageNum, pageSize));
    }

    @Operation(summary = "Clear browse history")
    @DeleteMapping
    public Result<Void> clear() {
        browseHistoryService.clearHistory();
        return Result.success();
    }
}
