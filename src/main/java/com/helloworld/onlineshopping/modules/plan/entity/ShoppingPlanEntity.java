package com.helloworld.onlineshopping.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.helloworld.onlineshopping.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shopping_plan")
public class ShoppingPlanEntity extends BaseEntity {
    private Long userId;
    private String planName;
    private LocalDateTime triggerTime;
    private Integer planStatus;
    private BigDecimal budgetAmount;
    private String remark;
}
