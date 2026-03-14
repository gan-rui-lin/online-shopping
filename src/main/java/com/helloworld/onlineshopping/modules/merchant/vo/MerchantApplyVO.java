package com.helloworld.onlineshopping.modules.merchant.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MerchantApplyVO {
    private Long id;
    private Long userId;
    private String username;
    private String shopName;
    private String businessLicenseNo;
    private String contactName;
    private String contactPhone;
    private Integer applyStatus;
    private String remark;
    private LocalDateTime createTime;
}
