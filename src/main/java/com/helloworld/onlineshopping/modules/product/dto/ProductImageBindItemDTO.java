package com.helloworld.onlineshopping.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductImageBindItemDTO {

    /**
     * 1=spu_main, 2=spu_detail, 3=sku
     */
    @NotNull
    private Integer imageType;

    private Long skuId;

    @NotBlank
    private String imageUrl;

    private Integer sortOrder;
}

