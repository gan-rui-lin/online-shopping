package com.helloworld.onlineshopping.modules.admin.controller;

import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.admin.service.AdminService;
import com.helloworld.onlineshopping.modules.admin.vo.DashboardVO;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
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
    private final ProductSpuMapper spuMapper;

    @Operation(summary = "Get dashboard statistics")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(adminService.getDashboard());
    }

    @Operation(summary = "Approve product audit")
    @PostMapping("/product/{spuId}/approve")
    public Result<Void> approveProduct(@PathVariable Long spuId) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu != null) {
            spu.setAuditStatus(1);
            spuMapper.updateById(spu);
        }
        return Result.success();
    }

    @Operation(summary = "Reject product audit")
    @PostMapping("/product/{spuId}/reject")
    public Result<Void> rejectProduct(@PathVariable Long spuId) {
        ProductSpuEntity spu = spuMapper.selectById(spuId);
        if (spu != null) {
            spu.setAuditStatus(2);
            spuMapper.updateById(spu);
        }
        return Result.success();
    }
}
