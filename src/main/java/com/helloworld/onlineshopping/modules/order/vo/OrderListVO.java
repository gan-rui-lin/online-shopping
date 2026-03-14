package com.helloworld.onlineshopping.modules.order.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderListVO {
    private String orderNo;
    private Long shopId;
    private String shopName;
    private Integer orderStatus;
    private BigDecimal payAmount;
    private LocalDateTime createTime;
    private List<OrderItemVO> itemList;
}
