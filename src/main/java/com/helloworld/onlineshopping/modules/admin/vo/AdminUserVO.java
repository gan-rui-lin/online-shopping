package com.helloworld.onlineshopping.modules.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminUserVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Integer status;
    private Integer userType;
    private List<String> roles;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}
