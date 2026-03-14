package com.helloworld.onlineshopping.modules.plan.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShoppingPlanItemVO {
    private Long id;
    private String keyword;
    private Long categoryId;
    private BigDecimal expectedPriceMin;
    private BigDecimal expectedPriceMax;
    private Integer quantity;
    private Long matchedSpuId;
    private String matchedProductTitle;
}
