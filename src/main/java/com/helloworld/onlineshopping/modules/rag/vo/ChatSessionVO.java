package com.helloworld.onlineshopping.modules.rag.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionVO {
    private Long sessionId;
    private String title;
    private String sessionType;
    private Long spuId;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;
}
