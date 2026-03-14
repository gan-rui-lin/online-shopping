package com.helloworld.onlineshopping.modules.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecordEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String orderNo;
    private String payNo;
    private Long userId;
    private BigDecimal payAmount;
    private Integer payMethod;
    private Integer payStatus;
    private String thirdTradeNo;
    private LocalDateTime payTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
