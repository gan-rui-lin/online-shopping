package com.helloworld.onlineshopping.modules.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentTaskCreateDTO {
    @NotBlank(message = "Task type is required")
    private String taskType;

    private Long requiredCategoryId;

    private String requiredCategoryName;

    private String frequency;

    private Long bindSpuId;

    private Integer quantity;

    private String intentRequirement;

    private String preference;

    private BigDecimal budgetLimit;

    private String locale;
}
