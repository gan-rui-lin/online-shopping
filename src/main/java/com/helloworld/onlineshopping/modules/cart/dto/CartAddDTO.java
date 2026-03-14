package com.helloworld.onlineshopping.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartAddDTO {
    @NotNull(message = "SKU ID is required")
    private Long skuId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;
}
