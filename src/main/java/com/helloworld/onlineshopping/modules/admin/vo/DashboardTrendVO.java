package com.helloworld.onlineshopping.modules.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardTrendVO {
    private String date;
    private BigDecimal value;
}
