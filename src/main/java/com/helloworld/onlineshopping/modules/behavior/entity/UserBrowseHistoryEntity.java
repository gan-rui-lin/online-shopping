package com.helloworld.onlineshopping.modules.behavior.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_browse_history")
public class UserBrowseHistoryEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long spuId;
    private LocalDateTime browseTime;
}
