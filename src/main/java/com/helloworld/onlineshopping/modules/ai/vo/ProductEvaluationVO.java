package com.helloworld.onlineshopping.modules.ai.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProductEvaluationVO {
    private Long spuId;
    private String overallLevel;
    private Double qualityScore;
    private Double valueScore;
    private String scenarioFit;
    private List<String> potentialRisks;
    private String summary;
}
