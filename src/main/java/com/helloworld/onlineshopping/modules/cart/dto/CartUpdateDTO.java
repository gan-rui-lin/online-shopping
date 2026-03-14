package com.helloworld.onlineshopping.modules.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartUpdateDTO {
    @NotNull(message = "SKU ID is required")
    private Long skuId;
    private Integer quantity;
    private Integer checked;
}
