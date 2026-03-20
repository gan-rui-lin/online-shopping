package com.helloworld.onlineshopping.modules.order.service.batch;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.helloworld.onlineshopping.modules.order.entity.OrderItemEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderItemMapper;
import org.springframework.stereotype.Service;

@Service
public class OrderItemBatchService extends ServiceImpl<OrderItemMapper, OrderItemEntity> {
}
