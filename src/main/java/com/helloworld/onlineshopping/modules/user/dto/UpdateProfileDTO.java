package com.helloworld.onlineshopping.modules.user.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String nickname;
    private String email;
    private String avatarUrl;
    private String phone;
}
