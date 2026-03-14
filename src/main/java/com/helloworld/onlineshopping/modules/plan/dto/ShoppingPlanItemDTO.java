package com.helloworld.onlineshopping.modules.plan.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShoppingPlanItemDTO {
    private String keyword;
    private Long categoryId;
    private BigDecimal expectedPriceMin;
    private BigDecimal expectedPriceMax;
    private Integer quantity = 1;
}
