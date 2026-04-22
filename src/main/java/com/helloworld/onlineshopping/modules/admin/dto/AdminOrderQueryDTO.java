package com.helloworld.onlineshopping.modules.admin.dto;

import com.helloworld.onlineshopping.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminOrderQueryDTO extends PageQuery {
    private String orderNo;
    private Long userId;
    private Long shopId;
    private Integer orderStatus;
}
