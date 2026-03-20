package com.helloworld.onlineshopping.modules.order.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.modules.order.entity.OrderEntity;
import com.helloworld.onlineshopping.modules.order.mapper.OrderMapper;
import com.helloworld.onlineshopping.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderMapper orderMapper;
    private final OrderService orderService;

    private static final String ORDER_TIMEOUT_KEY = "order:timeout:zset";

    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutOrders() {
        long now = System.currentTimeMillis();
        Set<Object> expiredOrders = redisTemplate.opsForZSet().rangeByScore(ORDER_TIMEOUT_KEY, 0, now);
        if (expiredOrders == null || expiredOrders.isEmpty()) return;

        for (Object orderNoObj : expiredOrders) {
            String orderNo = orderNoObj.toString();
            try {
                OrderEntity order = orderMapper.selectOne(
                    new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderNo, orderNo));
                if (order != null && order.getOrderStatus() == 0) {
                    log.info("Auto cancelling timeout order: {}", orderNo);
                    orderService.cancelTimeoutOrderByOrderNo(orderNo);
                } else {
                    redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, orderNo);
                }
            } catch (Exception e) {
                log.error("Failed to cancel timeout order: {}", orderNo, e);
            }
        }
    }
}
