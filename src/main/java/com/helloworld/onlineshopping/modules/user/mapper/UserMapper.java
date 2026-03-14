package com.helloworld.onlineshopping.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.helloworld.onlineshopping.modules.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
