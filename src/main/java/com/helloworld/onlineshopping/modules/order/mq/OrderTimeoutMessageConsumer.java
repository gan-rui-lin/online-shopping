package com.helloworld.onlineshopping.modules.order.mq;

import com.helloworld.onlineshopping.common.mq.RabbitMQConfig;
import com.helloworld.onlineshopping.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutMessageConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_TIMEOUT_QUEUE)
    public void consumeOrderTimeout(String orderNo) {
        log.info("Receive timeout message for orderNo={}", orderNo);
        orderService.cancelTimeoutOrderByOrderNo(orderNo);
    }
}
