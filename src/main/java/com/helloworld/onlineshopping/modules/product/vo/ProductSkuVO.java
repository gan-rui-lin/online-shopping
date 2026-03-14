package com.helloworld.onlineshopping.modules.product.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSkuVO {
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String specJson;
    private BigDecimal salePrice;
    private BigDecimal originPrice;
    private Integer stock;
    private String imageUrl;
}
