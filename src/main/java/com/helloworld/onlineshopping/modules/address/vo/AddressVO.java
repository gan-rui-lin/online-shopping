package com.helloworld.onlineshopping.modules.address.vo;

import lombok.Data;

@Data
public class AddressVO {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String fullAddress;
    private Integer isDefault;
    private String tagName;
}
