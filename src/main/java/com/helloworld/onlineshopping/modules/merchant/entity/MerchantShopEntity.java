package com.helloworld.onlineshopping.modules.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.helloworld.onlineshopping.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant_shop")
public class MerchantShopEntity extends BaseEntity {
    private Long userId;
    private String shopName;
    private String shopLogo;
    private String shopDesc;
    private Integer shopStatus;
    private BigDecimal score;
}
