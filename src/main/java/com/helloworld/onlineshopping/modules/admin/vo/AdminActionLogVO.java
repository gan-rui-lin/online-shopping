package com.helloworld.onlineshopping.modules.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminActionLogVO {
    private Long id;
    private Long operatorId;
    private String operatorName;
    private String module;
    private String action;
    private String targetType;
    private String targetId;
    private String detail;
    private Integer success;
    private LocalDateTime createTime;
}
