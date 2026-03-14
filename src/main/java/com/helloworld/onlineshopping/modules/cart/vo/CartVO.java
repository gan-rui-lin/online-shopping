package com.helloworld.onlineshopping.modules.cart.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartVO {
    private List<CartItemVO> items;
    private Integer totalCount;
    private BigDecimal totalAmount;
    private Integer checkedCount;
    private BigDecimal checkedAmount;
}
