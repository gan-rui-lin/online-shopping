package com.helloworld.onlineshopping.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ProductSpuUpdateDTO {
    private Long categoryId;
    private String brandName;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    private String subTitle;
    private String mainImage;
    private String detailText;
    private List<String> imageList;
    private List<ProductSkuDTO> skuList;
}
