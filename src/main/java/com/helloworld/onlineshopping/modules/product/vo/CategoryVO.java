package com.helloworld.onlineshopping.modules.product.vo;

import lombok.Data;
import java.util.List;

@Data
public class CategoryVO {
    private Long id;
    private Long parentId;
    private String categoryName;
    private Integer level;
    private List<CategoryVO> children;
}
