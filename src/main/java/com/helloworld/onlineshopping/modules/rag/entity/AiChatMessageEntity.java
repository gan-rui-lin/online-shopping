package com.helloworld.onlineshopping.modules.rag.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("ai_chat_message")
public class AiChatMessageEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
