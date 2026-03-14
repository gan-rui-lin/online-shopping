package com.helloworld.onlineshopping.modules.review.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.helloworld.onlineshopping.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_review")
public class ProductReviewEntity extends BaseEntity {
    private Long orderItemId;
    private String orderNo;
    private Long userId;
    private Long spuId;
    private Long skuId;
    private Integer score;
    private String content;
    private String imageUrls;
    private Integer anonymousFlag;
    private String replyContent;
    private LocalDateTime replyTime;
    private Integer reviewStatus;
}
