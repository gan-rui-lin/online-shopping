package com.helloworld.onlineshopping.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayStatusEnum {
    UNPAID(0, "Unpaid"),
    PAID(1, "Paid"),
    REFUNDED(2, "Refunded");

    private final int code;
    private final String desc;
}
