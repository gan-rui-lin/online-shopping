package com.helloworld.onlineshopping.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.modules.admin.vo.DashboardVO;
import com.helloworld.onlineshopping.modules.merchant.entity.MerchantShopEntity;
import com.helloworld.onlineshopping.modules.merchant.mapper.MerchantShopMapper;
import com.helloworld.onlineshopping.modules.order.entity.OrderEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderMapper;
import com.helloworld.onlineshopping.modules.product.entity.ProductSpuEntity;
import com.helloworld.onlineshopping.modules.product.mapper.ProductSpuMapper;
import com.helloworld.onlineshopping.modules.user.entity.UserEntity;
import com.helloworld.onlineshopping.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final MerchantShopMapper shopMapper;
    private final ProductSpuMapper spuMapper;
    private final OrderMapper orderMapper;

    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setMerchantCount(shopMapper.selectCount(null));
        vo.setProductCount(spuMapper.selectCount(
            new LambdaQueryWrapper<ProductSpuEntity>().eq(ProductSpuEntity::getStatus, 1)));
        vo.setOrderCount(orderMapper.selectCount(null));

        // Calculate GMV (sum of paid orders)
        List<OrderEntity> paidOrders = orderMapper.selectList(
            new LambdaQueryWrapper<OrderEntity>().ne(OrderEntity::getPayStatus, 0));
        BigDecimal gmv = paidOrders.stream()
            .map(OrderEntity::getPayAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setGmv(gmv);

        // Today's order count
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayOrderCount(orderMapper.selectCount(
            new LambdaQueryWrapper<OrderEntity>().ge(OrderEntity::getCreateTime, todayStart)));

        return vo;
    }
}
