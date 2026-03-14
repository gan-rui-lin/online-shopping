package com.helloworld.onlineshopping.modules.recommendation.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RecommendProductVO {
    private Long spuId;
    private String title;
    private String mainImage;
    private BigDecimal minPrice;
    private Integer salesCount;
    private Double score;
    private String reason;
}
