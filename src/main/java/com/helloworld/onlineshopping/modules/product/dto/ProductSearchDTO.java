package com.helloworld.onlineshopping.modules.product.dto;

import com.helloworld.onlineshopping.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSearchDTO extends PageQuery {
    private String keyword;
    private Long categoryId;
    private String brandName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortField;
    private String sortOrder;
}
