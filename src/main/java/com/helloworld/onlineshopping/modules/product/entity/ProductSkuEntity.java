package com.helloworld.onlineshopping.modules.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.helloworld.onlineshopping.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_sku")
public class ProductSkuEntity extends BaseEntity {
    private Long spuId;
    private String skuCode;
    private String skuName;
    private BigDecimal salePrice;
    private BigDecimal originPrice;
    private Integer stock;
    private Integer lockStock;
    private Integer warningStock;
    private String imageUrl;
    private String specJson;
    private Integer status;
    @Version
    private Integer version;
}
