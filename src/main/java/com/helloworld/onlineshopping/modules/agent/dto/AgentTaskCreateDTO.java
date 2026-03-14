package com.helloworld.onlineshopping.modules.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentTaskCreateDTO {
    @NotBlank(message = "Prompt is required")
    private String userPrompt;
}
