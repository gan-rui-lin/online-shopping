package com.helloworld.onlineshopping.modules.agent.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AgentRecommendationVO {
    private Long spuId;
    private Long skuId;
    private String title;
    private String mainImage;
    private BigDecimal price;
    private String reason;
}
