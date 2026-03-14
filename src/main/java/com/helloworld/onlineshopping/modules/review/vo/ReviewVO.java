package com.helloworld.onlineshopping.modules.review.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO {
    private Long reviewId;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer anonymousFlag;
    private Integer score;
    private String content;
    private List<String> imageUrls;
    private String skuName;
    private String replyContent;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
}
