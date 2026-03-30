package com.helloworld.onlineshopping.modules.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_record")
public class FileRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;

    private String originalName;

    private String fileType;

    private Long fileSize;

    private String url;

    private String thumbnailUrl;

    private String category; // avatar, product, etc.

    private String uploaderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
