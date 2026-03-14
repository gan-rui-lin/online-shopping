package com.helloworld.onlineshopping.modules.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShopUpdateDTO {
    @NotBlank(message = "Shop name cannot be empty")
    private String shopName;

    private String shopLogo;
    private String shopDesc;
}
