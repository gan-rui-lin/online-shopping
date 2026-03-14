package com.helloworld.onlineshopping.modules.order.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentVO {
    private String orderNo;
    private String payNo;
    private Integer payStatus;
    private BigDecimal amount;
    private LocalDateTime payTime;
}
