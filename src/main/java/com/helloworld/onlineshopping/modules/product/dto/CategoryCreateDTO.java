package com.helloworld.onlineshopping.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateDTO {
    private Long parentId = 0L;
    @NotBlank(message = "Category name is required")
    private String categoryName;
    private Integer sortOrder = 0;
}
