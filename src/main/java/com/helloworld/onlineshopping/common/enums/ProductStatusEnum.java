package com.helloworld.onlineshopping.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatusEnum {
    DRAFT(0, "Draft"),
    ON_SHELF(1, "On Shelf"),
    OFF_SHELF(2, "Off Shelf");

    private final int code;
    private final String desc;
}
