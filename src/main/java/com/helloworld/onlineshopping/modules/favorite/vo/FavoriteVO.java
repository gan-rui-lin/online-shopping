package com.helloworld.onlineshopping.modules.favorite.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteVO {
    private Long spuId;
    private String title;
    private String mainImage;
    private BigDecimal minPrice;
    private String shopName;
    private LocalDateTime createTime;
}
