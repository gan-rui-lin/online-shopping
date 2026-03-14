package com.helloworld.onlineshopping.modules.cart.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.helloworld.onlineshopping.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart_item")
public class CartItemEntity extends BaseEntity {
    private Long userId;
    private Long skuId;
    private Integer quantity;
    private Integer checked;
}
