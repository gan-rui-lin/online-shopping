package com.helloworld.onlineshopping.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.helloworld.onlineshopping.modules.order.entity.OrderOperateLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderOperateLogMapper extends BaseMapper<OrderOperateLogEntity> {
}
