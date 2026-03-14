package com.helloworld.onlineshopping.modules.review.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReviewStatisticVO {
    private Long spuId;
    private Integer totalCount;
    private Integer goodCount;
    private Integer mediumCount;
    private Integer badCount;
    private BigDecimal goodRate;
}
