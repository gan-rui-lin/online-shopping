package com.helloworld.onlineshopping.modules.ai.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReviewSummaryVO {
    private Long spuId;
    private Integer totalReviews;
    private Double averageScore;
    private List<String> pros;
    private List<String> cons;
    private String summary;
}
