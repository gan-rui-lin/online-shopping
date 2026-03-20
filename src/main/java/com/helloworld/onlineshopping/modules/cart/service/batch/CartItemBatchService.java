package com.helloworld.onlineshopping.modules.cart.service.batch;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.helloworld.onlineshopping.modules.cart.entity.CartItemEntity;
import com.helloworld.onlineshopping.modules.cart.mapper.CartItemMapper;
import org.springframework.stereotype.Service;

@Service
public class CartItemBatchService extends ServiceImpl<CartItemMapper, CartItemEntity> {
}
