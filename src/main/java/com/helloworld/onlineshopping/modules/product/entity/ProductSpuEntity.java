package com.helloworld.onlineshopping.modules.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.helloworld.onlineshopping.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_spu")
public class ProductSpuEntity extends BaseEntity {
    private Long shopId;
    private Long categoryId;
    private String brandName;
    private String title;
    private String subTitle;
    private String mainImage;
    private String detailText;
    /** 0draft 1on_shelf 2off_shelf */
    private Integer status;
    /** 0pending 1approved 2rejected */
    private Integer auditStatus;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer salesCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer browseCount;
}
