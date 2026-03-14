package com.helloworld.onlineshopping.modules.merchant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantAuditDTO {
    @NotNull(message = "Audit status is required")
    private Integer auditStatus;
    private String remark;
}
