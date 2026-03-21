package com.helloworld.onlineshopping.modules.file.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private String url;

    private String thumbnailUrl;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private LocalDateTime uploadTime;

    private String category;
}
