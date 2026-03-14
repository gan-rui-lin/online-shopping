package com.helloworld.onlineshopping.modules.product.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailVO {
    private Long spuId;
    private String title;
    private String subTitle;
    private String brandName;
    private String mainImage;
    private String detailText;
    private Integer status;
    private Long shopId;
    private String shopName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer salesCount;
    private Integer favoriteCount;
    private List<String> imageList;
    private List<ProductSkuVO> skuList;
}
