package com.helloworld.onlineshopping.modules.order.dto;

import com.helloworld.onlineshopping.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQueryDTO extends PageQuery {
    private Integer orderStatus;
}
