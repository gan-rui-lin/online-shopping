package com.helloworld.onlineshopping.modules.admin.controller;

import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.common.security.SecurityUtil;
import com.helloworld.onlineshopping.modules.admin.dto.AdminActionLogQueryDTO;
import com.helloworld.onlineshopping.modules.admin.dto.AdminOrderQueryDTO;
import com.helloworld.onlineshopping.modules.admin.service.AdminService;
import com.helloworld.onlineshopping.modules.admin.dto.AdminUserQueryDTO;
import com.helloworld.onlineshopping.modules.admin.service.AdminActionLogService;
import com.helloworld.onlineshopping.modules.admin.vo.AdminActionLogVO;
import com.helloworld.onlineshopping.modules.admin.vo.AdminOrderVO;
import com.helloworld.onlineshopping.modules.admin.vo.AdminUserVO;
import com.helloworld.onlineshopping.modules.admin.vo.DashboardVO;
import com.helloworld.onlineshopping.modules.admin.vo.SecurityOverviewVO;
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
    private final AdminActionLogService adminActionLogService;
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
        executeWithLog("PRODUCT_AUDIT", "APPROVE", "PRODUCT", String.valueOf(spuId),
            "Approve pending product", () -> productService.approveProduct(spuId));
        return Result.success();
    }

    @Operation(summary = "Reject product audit")
    @PostMapping("/product/{spuId}/reject")
    public Result<Void> rejectProduct(@PathVariable Long spuId) {
        executeWithLog("PRODUCT_AUDIT", "REJECT", "PRODUCT", String.valueOf(spuId),
            "Reject pending product", () -> productService.rejectProduct(spuId));
        return Result.success();
    }

    @Operation(summary = "List members")
    @GetMapping("/users")
    public Result<PageResult<AdminUserVO>> users(AdminUserQueryDTO dto) {
        return Result.success(adminService.getUsers(dto));
    }

    @Operation(summary = "Update member status")
    @PutMapping("/users/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        executeWithLog("MEMBER", "UPDATE_STATUS", "USER", String.valueOf(userId),
            "Update user status to " + status,
            () -> adminService.updateUserStatus(userId, status));
        return Result.success();
    }

    @Operation(summary = "List all orders for intervention")
    @GetMapping("/orders")
    public Result<PageResult<AdminOrderVO>> orders(AdminOrderQueryDTO dto) {
        return Result.success(adminService.getOrders(dto));
    }

    @Operation(summary = "Cancel unpaid order by admin")
    @PostMapping("/orders/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo, @RequestParam(required = false) String reason) {
        executeWithLog("ORDER_INTERVENTION", "CANCEL_ORDER", "ORDER", orderNo,
            "Cancel unpaid order by admin",
            () -> adminService.cancelUnpaidOrder(orderNo, reason));
        return Result.success();
    }

    @Operation(summary = "Approve refund by admin")
    @PostMapping("/orders/{orderNo}/refund/approve")
    public Result<Void> approveRefund(@PathVariable String orderNo) {
        executeWithLog("ORDER_INTERVENTION", "APPROVE_REFUND", "ORDER", orderNo,
            "Approve refund by admin",
            () -> adminService.approveRefundByAdmin(orderNo));
        return Result.success();
    }

    @Operation(summary = "Reject refund by admin")
    @PostMapping("/orders/{orderNo}/refund/reject")
    public Result<Void> rejectRefund(@PathVariable String orderNo, @RequestParam String reason) {
        executeWithLog("ORDER_INTERVENTION", "REJECT_REFUND", "ORDER", orderNo,
            "Reject refund by admin",
            () -> adminService.rejectRefundByAdmin(orderNo, reason));
        return Result.success();
    }

    @Operation(summary = "List admin action logs")
    @GetMapping("/action-logs")
    public Result<PageResult<AdminActionLogVO>> actionLogs(AdminActionLogQueryDTO dto) {
        return Result.success(adminActionLogService.list(dto));
    }

    @Operation(summary = "Get security overview")
    @GetMapping("/security/overview")
    public Result<SecurityOverviewVO> securityOverview() {
        return Result.success(adminService.getSecurityOverview());
    }

    private void executeWithLog(String module, String action, String targetType, String targetId,
                                String detail, Runnable task) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        String operatorName = SecurityUtil.getCurrentUser().getUsername();
        try {
            task.run();
            adminActionLogService.record(operatorId, operatorName, module, action, targetType, targetId, detail, true);
        } catch (RuntimeException e) {
            adminActionLogService.record(operatorId, operatorName, module, action, targetType, targetId,
                detail + "; reason=" + e.getMessage(), false);
            throw e;
        }
    }
}
