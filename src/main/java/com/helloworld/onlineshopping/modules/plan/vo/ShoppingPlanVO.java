package com.helloworld.onlineshopping.modules.plan.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShoppingPlanVO {
    private Long planId;
    private String planName;
    private LocalDateTime triggerTime;
    private Integer planStatus;
    private BigDecimal budgetAmount;
    private String remark;
    private LocalDateTime createTime;
    private List<ShoppingPlanItemVO> items;
}
