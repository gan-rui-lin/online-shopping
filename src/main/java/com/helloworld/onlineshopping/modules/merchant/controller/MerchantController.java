package com.helloworld.onlineshopping.modules.merchant.controller;

import com.helloworld.onlineshopping.common.api.Result;
import com.helloworld.onlineshopping.modules.merchant.dto.MerchantApplyDTO;
import com.helloworld.onlineshopping.modules.merchant.dto.MerchantAuditDTO;
import com.helloworld.onlineshopping.modules.merchant.dto.ShopUpdateDTO;
import com.helloworld.onlineshopping.modules.merchant.service.MerchantService;
import com.helloworld.onlineshopping.modules.merchant.vo.MerchantApplyVO;
import com.helloworld.onlineshopping.modules.merchant.vo.MerchantShopVO;
import com.helloworld.onlineshopping.modules.merchant.vo.ShopStatisticVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Merchant", description = "Merchant APIs")
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "Apply to become merchant")
    @PostMapping("/apply")
    public Result<Void> apply(@Valid @RequestBody MerchantApplyDTO dto) {
        merchantService.apply(dto);
        return Result.success();
    }

    @Operation(summary = "Get current merchant shop")
    @GetMapping("/shop/current")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<MerchantShopVO> currentShop() {
        return Result.success(merchantService.getCurrentShop());
    }

    @Operation(summary = "Get pending applications (admin)")
    @GetMapping("/apply/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<List<MerchantApplyVO>> applyList() {
        return Result.success(merchantService.getPendingApplyList());
    }

    @Operation(summary = "Audit merchant application (admin)")
    @PostMapping("/apply/{id}/audit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody MerchantAuditDTO dto) {
        merchantService.audit(id, dto);
        return Result.success();
    }

    @Operation(summary = "Update shop info (merchant)")
    @PutMapping("/shop")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<Void> updateShop(@Valid @RequestBody ShopUpdateDTO dto) {
        merchantService.updateShop(dto);
        return Result.success();
    }

    @Operation(summary = "Get shop statistics (merchant)")
    @GetMapping("/shop/statistics")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public Result<ShopStatisticVO> getStatistics() {
        return Result.success(merchantService.getShopStatistics());
    }
}
