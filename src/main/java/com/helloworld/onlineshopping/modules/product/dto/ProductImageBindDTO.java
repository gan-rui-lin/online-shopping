package com.helloworld.onlineshopping.modules.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProductImageBindDTO {

    /**
     * Optional override for product_spu.main_image.
     */
    private String mainImageUrl;

    @Valid
    @NotEmpty
    private List<ProductImageBindItemDTO> images;
}

