package com.helloworld.onlineshopping.modules.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.helloworld.onlineshopping.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_info")
public class OrderEntity extends BaseEntity {
    private String orderNo;
    private Long userId;
    private Long shopId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    /** 0unpaid 1to_ship 2to_receive 3completed 4cancelled 5refunding 6refunded */
    private Integer orderStatus;
    /** 0unpaid 1paid 2refunded */
    private Integer payStatus;
    /** 1normal 2agent 3plan */
    private Integer sourceType;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
}
