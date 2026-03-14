package com.helloworld.onlineshopping.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ProductSpuCreateDTO {
    @NotNull(message = "Category is required")
    private Long categoryId;
    private String brandName;
    @NotBlank(message = "Title is required")
    private String title;
    private String subTitle;
    private String mainImage;
    private String detailText;
    private List<ProductSkuDTO> skuList;
    private List<String> imageList;
}
