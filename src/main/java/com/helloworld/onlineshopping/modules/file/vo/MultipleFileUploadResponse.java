package com.helloworld.onlineshopping.modules.file.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipleFileUploadResponse {

    private List<FileUploadResponse> files;

    private Integer totalCount;

    private LocalDateTime uploadTime;
}
