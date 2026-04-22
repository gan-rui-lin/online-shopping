package com.helloworld.onlineshopping.modules.admin.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardVO {
    private Long userCount;
    private Long merchantCount;
    private Long productCount;
    private Long orderCount;
    private BigDecimal gmv;
    private Long todayOrderCount;
    private List<DashboardTrendVO> orderTrend;
    private List<DashboardTrendVO> gmvTrend;
    private List<OrderStatusStatVO> orderStatusStats;
}
