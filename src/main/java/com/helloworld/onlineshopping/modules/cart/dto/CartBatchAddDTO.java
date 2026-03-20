package com.helloworld.onlineshopping.modules.cart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CartBatchAddDTO {
    @NotEmpty(message = "Cart items are required")
    private List<@Valid CartAddDTO> items;
}
