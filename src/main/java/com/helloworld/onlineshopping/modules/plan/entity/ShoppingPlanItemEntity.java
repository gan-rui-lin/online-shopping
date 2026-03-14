package com.helloworld.onlineshopping.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shopping_plan_item")
public class ShoppingPlanItemEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long planId;
    private String keyword;
    private Long categoryId;
    private BigDecimal expectedPriceMin;
    private BigDecimal expectedPriceMax;
    private Integer quantity;
    private Long matchedSpuId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
