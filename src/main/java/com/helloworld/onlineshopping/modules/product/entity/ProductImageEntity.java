package com.helloworld.onlineshopping.modules.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_image")
public class ProductImageEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long spuId;
    private Long skuId;
    private String imageUrl;
    private Integer imageType;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
