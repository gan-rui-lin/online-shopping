package com.helloworld.onlineshopping.modules.order.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderSubmitVO {
    private String orderNo;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal freightAmount;
    private BigDecimal payAmount;
}
