package com.helloworld.onlineshopping.modules.merchant.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MerchantShopVO {
    private Long shopId;
    private Long userId;
    private String shopName;
    private String shopLogo;
    private String shopDesc;
    private Integer shopStatus;
    private BigDecimal score;
}
