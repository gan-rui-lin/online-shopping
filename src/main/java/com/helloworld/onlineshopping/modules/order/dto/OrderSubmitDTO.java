package com.helloworld.onlineshopping.modules.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class OrderSubmitDTO {
    @NotNull(message = "Address is required")
    private Long addressId;
    private String remark;
    @NotEmpty(message = "Please select items to order")
    private List<Long> cartSkuIds;
}
