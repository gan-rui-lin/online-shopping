package com.helloworld.onlineshopping.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.helloworld.onlineshopping.modules.order.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
}
