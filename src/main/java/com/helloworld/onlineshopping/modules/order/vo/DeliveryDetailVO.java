package com.helloworld.onlineshopping.modules.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryDetailVO {
    private String orderNo;
    private String trackingNo;
    private String carrier;
    private Integer status;
    private LocalDateTime deliveryTime;
    private LocalDateTime estimatedTime;
    private String currentLocation;
}
