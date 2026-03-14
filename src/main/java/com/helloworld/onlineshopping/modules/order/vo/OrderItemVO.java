package com.helloworld.onlineshopping.modules.order.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Long spuId;
    private Long skuId;
    private String productTitle;
    private String skuName;
    private String skuSpecJson;
    private String productImage;
    private BigDecimal salePrice;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer reviewStatus;
}
