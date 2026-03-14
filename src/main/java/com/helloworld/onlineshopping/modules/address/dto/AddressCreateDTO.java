package com.helloworld.onlineshopping.modules.address.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressCreateDTO {
    @NotBlank(message = "Receiver name is required")
    private String receiverName;
    @NotBlank(message = "Receiver phone is required")
    private String receiverPhone;
    @NotBlank(message = "Province is required")
    private String province;
    @NotBlank(message = "City is required")
    private String city;
    @NotBlank(message = "District is required")
    private String district;
    @NotBlank(message = "Detail address is required")
    private String detailAddress;
    private String postalCode;
    private Integer isDefault = 0;
    private String tagName;
}
