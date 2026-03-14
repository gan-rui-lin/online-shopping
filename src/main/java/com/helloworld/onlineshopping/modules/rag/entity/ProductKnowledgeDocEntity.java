package com.helloworld.onlineshopping.modules.rag.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("product_knowledge_doc")
public class ProductKnowledgeDocEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long spuId;
    private String title;
    private String content;
    private String sourceType;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
