package com.helloworld.onlineshopping.modules.auth.vo;

import lombok.Data;
import java.util.List;

@Data
public class LoginVO {
    private String token;
    private String tokenHead;
    private Long userId;
    private String username;
    private String nickname;
    private Integer userType;
    private List<String> roles;
}
