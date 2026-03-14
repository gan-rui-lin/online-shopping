package com.helloworld.onlineshopping.modules.address.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressUpdateDTO {
    @NotNull(message = "Address ID is required")
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String postalCode;
    private Integer isDefault;
    private String tagName;
}
