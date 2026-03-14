package com.helloworld.onlineshopping.modules.agent.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class AgentAddToCartDTO {
    @NotEmpty
    private List<Long> skuIds;
}
