package com.helloworld.onlineshopping.modules.file.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignedUrlResponse {

    private String signedUrl;

    private String fileName;

    private Long expirationTime; // 过期时间戳
}
