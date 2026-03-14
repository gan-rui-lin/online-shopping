package com.helloworld.onlineshopping.modules.plan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShoppingPlanCreateDTO {
    @NotBlank(message = "Plan name is required")
    private String planName;
    private LocalDateTime triggerTime;
    private BigDecimal budgetAmount;
    private String remark;
    private List<ShoppingPlanItemDTO> items;
}
