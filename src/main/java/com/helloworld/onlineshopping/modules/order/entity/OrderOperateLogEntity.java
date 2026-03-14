package com.helloworld.onlineshopping.modules.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("order_operate_log")
public class OrderOperateLogEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Integer beforeStatus;
    private Integer afterStatus;
    private Long operatorId;
    private String operatorRole;
    private String operateType;
    private String remark;
    private LocalDateTime operateTime;
}
