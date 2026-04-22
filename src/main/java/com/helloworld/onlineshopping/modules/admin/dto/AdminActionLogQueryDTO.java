package com.helloworld.onlineshopping.modules.admin.dto;

import com.helloworld.onlineshopping.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminActionLogQueryDTO extends PageQuery {
    private Long operatorId;
    private String module;
    private Integer success;
}
