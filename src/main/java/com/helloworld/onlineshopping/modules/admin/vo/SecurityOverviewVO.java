package com.helloworld.onlineshopping.modules.admin.vo;

import lombok.Data;

import java.util.List;

@Data
public class SecurityOverviewVO {
    private Integer maxFailures;
    private Integer lockMinutes;
    private Long lockedAccountCount;
    private Long todayFailedLoginCount;
    private List<String> lockedAccounts;
}
