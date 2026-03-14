package com.helloworld.onlineshopping.modules.cart.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long skuId;
    private Long spuId;
    private String spuName;
    private String skuName;
    private String mainImage;
    private String specJson;
    private BigDecimal price;
    private Integer quantity;
    private Integer checked;
    private Integer stock;
    private Boolean available;
}
