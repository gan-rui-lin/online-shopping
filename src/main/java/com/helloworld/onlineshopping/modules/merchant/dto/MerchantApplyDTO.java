package com.helloworld.onlineshopping.modules.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MerchantApplyDTO {
    @NotBlank(message = "Shop name is required")
    private String shopName;
    private String businessLicenseNo;
    private String contactName;
    private String contactPhone;
}
