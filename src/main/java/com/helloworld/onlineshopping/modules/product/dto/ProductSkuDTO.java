package com.helloworld.onlineshopping.modules.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSkuDTO {
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String specJson;
    @NotNull(message = "Price is required")
    private BigDecimal price;
    private BigDecimal originPrice;
    @NotNull(message = "Stock is required")
    private Integer stock;
    private String imageUrl;
}
