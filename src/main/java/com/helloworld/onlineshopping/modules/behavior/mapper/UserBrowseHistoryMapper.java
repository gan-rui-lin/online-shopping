package com.helloworld.onlineshopping.modules.behavior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.helloworld.onlineshopping.modules.behavior.entity.UserBrowseHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserBrowseHistoryMapper extends BaseMapper<UserBrowseHistoryEntity> {
}
