package com.helloworld.onlineshopping.modules.product.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSimpleVO {
    private Long spuId;
    private String title;
    private String subTitle;
    private String mainImage;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer salesCount;
    private String shopName;
}
