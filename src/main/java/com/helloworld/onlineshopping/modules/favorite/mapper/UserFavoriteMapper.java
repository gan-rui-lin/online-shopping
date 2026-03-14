package com.helloworld.onlineshopping.modules.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.helloworld.onlineshopping.modules.favorite.entity.UserFavoriteEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavoriteEntity> {
}
