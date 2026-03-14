package com.helloworld.onlineshopping.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    UNPAID(0, "Unpaid"),
    TO_SHIP(1, "To Ship"),
    TO_RECEIVE(2, "To Receive"),
    COMPLETED(3, "Completed"),
    CANCELLED(4, "Cancelled"),
    REFUNDING(5, "Refunding"),
    REFUNDED(6, "Refunded");

    private final int code;
    private final String desc;

    public static OrderStatusEnum of(int code) {
        for (OrderStatusEnum status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("Invalid order status: " + code);
    }
}
