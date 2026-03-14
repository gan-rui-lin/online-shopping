package com.helloworld.onlineshopping.modules.rag.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ChatMessageVO {
    private String role;
    private String content;
    private LocalDateTime createTime;
}
