package com.helloworld.onlineshopping.modules.order.controller;

import com.helloworld.onlineshopping.common.api.PageResult;
import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.order.dto.OrderQueryDTO;
import com.helloworld.onlineshopping.modules.order.dto.OrderSubmitDTO;
import com.helloworld.onlineshopping.modules.order.dto.RefundRequestDTO;
import com.helloworld.onlineshopping.modules.order.service.OrderService;
import com.helloworld.onlineshopping.modules.order.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Order", description = "Order APIs")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Submit order")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@Valid @RequestBody OrderSubmitDTO dto) {
        return Result.success(orderService.submitOrder(dto));
    }

    @Operation(summary = "Get order detail")
    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        return Result.success(orderService.getOrderDetail(orderNo));
    }

    @Operation(summary = "Get order list")
    @GetMapping("/list")
    public Result<PageResult<OrderListVO>> list(OrderQueryDTO dto) {
        return Result.success(orderService.getOrderList(dto));
    }

    @Operation(summary = "Cancel order")
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo, @RequestParam(required = false) String reason) {
        orderService.cancelOrder(orderNo, reason);
        return Result.success();
    }

    @Operation(summary = "Pay order (simulated)")
    @PostMapping("/{orderNo}/pay")
    public Result<PaymentVO> pay(@PathVariable String orderNo) {
        return Result.success(orderService.payOrder(orderNo));
    }

    @Operation(summary = "Confirm receive")
    @PostMapping("/{orderNo}/confirm-receive")
    public Result<Void> confirmReceive(@PathVariable String orderNo) {
        orderService.confirmReceive(orderNo);
        return Result.success();
    }

    @Operation(summary = "Deliver order (merchant)")
    @PostMapping("/{orderNo}/deliver")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<Void> deliver(@PathVariable String orderNo) {
        orderService.deliverOrder(orderNo);
        return Result.success();
    }

    @Operation(summary = "Get merchant orders (merchant)")
    @GetMapping("/merchant/list")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<PageResult<OrderListVO>> getMerchantOrders(OrderQueryDTO dto) {
        return Result.success(orderService.getMerchantOrders(dto));
    }

    @Operation(summary = "Apply for refund")
    @PostMapping("/{orderNo}/refund/apply")
    public Result<Void> applyRefund(@PathVariable String orderNo, @Valid @RequestBody RefundRequestDTO dto) {
        orderService.applyRefund(orderNo, dto.getReason());
        return Result.success();
    }

    @Operation(summary = "Approve refund (merchant)")
    @PostMapping("/{orderNo}/refund/approve")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<Void> approveRefund(@PathVariable String orderNo) {
        orderService.approveRefund(orderNo);
        return Result.success();
    }

    @Operation(summary = "Reject refund (merchant)")
    @PostMapping("/{orderNo}/refund/reject")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<Void> rejectRefund(@PathVariable String orderNo, @RequestParam String reason) {
        orderService.rejectRefund(orderNo, reason);
        return Result.success();
    }

    @Operation(summary = "Get delivery details")
    @GetMapping("/{orderNo}/delivery")
    public Result<com.helloworld.onlineshopping.modules.order.vo.DeliveryDetailVO> getDeliveryDetails(@PathVariable String orderNo) {
        return Result.success(orderService.getDeliveryDetails(orderNo));
    }
}
