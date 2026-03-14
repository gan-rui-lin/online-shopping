package com.helloworld.onlineshopping.modules.merchant.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopStatisticVO {
    private Long shopId;
    private String shopName;
    private Integer totalProducts;
    private Integer onShelfProducts;
    private Integer totalOrders;
    private Integer pendingOrders;
    private BigDecimal totalRevenue;
    private BigDecimal score;
}
