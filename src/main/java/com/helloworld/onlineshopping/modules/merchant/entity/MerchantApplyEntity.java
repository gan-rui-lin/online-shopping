package com.helloworld.onlineshopping.modules.merchant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("merchant_apply")
public class MerchantApplyEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String shopName;
    private String businessLicenseNo;
    private String contactName;
    private String contactPhone;
    /** 0pending 1approved 2rejected */
    private Integer applyStatus;
    private String remark;
    private Long auditBy;
    private LocalDateTime auditTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
