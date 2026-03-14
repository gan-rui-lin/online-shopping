package com.helloworld.onlineshopping.modules.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.helloworld.onlineshopping.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class UserEntity extends BaseEntity {
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer status;
    private Integer userType;
    private LocalDateTime lastLoginTime;
}
