package com.helloworld.onlineshopping.modules.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundRequestDTO {
    @NotBlank(message = "Reason cannot be empty")
    private String reason;
}
