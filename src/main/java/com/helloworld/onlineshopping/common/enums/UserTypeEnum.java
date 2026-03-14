package com.helloworld.onlineshopping.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserTypeEnum {
    BUYER(1, "Buyer"),
    MERCHANT(2, "Merchant"),
    ADMIN(3, "Admin");

    private final int code;
    private final String desc;
}
