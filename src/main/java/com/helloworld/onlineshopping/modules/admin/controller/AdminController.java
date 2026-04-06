package com.helloworld.onlineshopping.modules.admin.controller;

import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.admin.service.AdminService;
import com.helloworld.onlineshopping.modules.admin.vo.DashboardVO;
import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.modules.product.service.ProductService;
import com.helloworld.onlineshopping.modules.product.vo.ProductSimpleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "Admin APIs")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;

    @Operation(summary = "Get dashboard statistics")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(adminService.getDashboard());
    }

    @Operation(summary = "List pending product audits")
    @GetMapping("/products/pending")
    public Result<PageResult<ProductSimpleVO>> pendingProducts(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productService.getPendingAuditProducts(pageNum, pageSize));
    }

    @Operation(summary = "Approve product audit")
    @PostMapping("/product/{spuId}/approve")
    public Result<Void> approveProduct(@PathVariable Long spuId) {
        productService.approveProduct(spuId);
        return Result.success();
    }

    @Operation(summary = "Reject product audit")
    @PostMapping("/product/{spuId}/reject")
    public Result<Void> rejectProduct(@PathVariable Long spuId) {
        productService.rejectProduct(spuId);
        return Result.success();
    }
}
