package com.helloworld.onlineshopping.modules.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("inventory_log")
public class InventoryLogEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long skuId;
    private String orderNo;
    private Integer changeCount;
    private Integer beforeStock;
    private Integer afterStock;
    private String operateType;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
