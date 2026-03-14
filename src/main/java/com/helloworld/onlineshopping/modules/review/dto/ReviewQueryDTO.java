package com.helloworld.onlineshopping.modules.review.dto;

import com.helloworld.onlineshopping.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewQueryDTO extends PageQuery {
    private Long spuId;
    private Integer score;
}
