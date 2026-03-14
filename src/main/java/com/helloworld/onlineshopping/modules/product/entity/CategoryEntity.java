package com.helloworld.onlineshopping.modules.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.helloworld.onlineshopping.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_category")
public class CategoryEntity extends BaseEntity {
    private Long parentId;
    private String categoryName;
    private Integer level;
    private Integer sortOrder;
    private String icon;
    private Integer status;
}
