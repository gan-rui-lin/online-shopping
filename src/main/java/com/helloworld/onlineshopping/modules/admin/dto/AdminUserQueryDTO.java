package com.helloworld.onlineshopping.modules.admin.dto;

import com.helloworld.onlineshopping.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserQueryDTO extends PageQuery {
    private String keyword;
    private Integer status;
    private Integer userType;
}
