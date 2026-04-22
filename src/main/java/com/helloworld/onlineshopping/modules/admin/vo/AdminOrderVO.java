package com.helloworld.onlineshopping.modules.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderVO {
    private String orderNo;
    private Long userId;
    private String username;
    private Long shopId;
    private String shopName;
    private Integer orderStatus;
    private Integer payStatus;
    private BigDecimal payAmount;
    private String cancelReason;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
}
