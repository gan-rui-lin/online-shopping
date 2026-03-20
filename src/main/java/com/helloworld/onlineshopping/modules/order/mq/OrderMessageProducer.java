package com.helloworld.onlineshopping.modules.order.mq;

import com.helloworld.onlineshopping.common.mq.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${order.timeout.delay-ms:1800000}")
    private long orderTimeoutDelayMs;

    public void sendOrderTimeoutMessage(String orderNo) {
        MessagePostProcessor processor = message -> {
            message.getMessageProperties().setExpiration(String.valueOf(orderTimeoutDelayMs));
            return message;
        };

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ORDER_EVENT_EXCHANGE,
            RabbitMQConfig.ORDER_TIMEOUT_ROUTING_KEY,
            orderNo,
            processor
        );
        log.info("Order timeout message sent, orderNo={}, delayMs={}", orderNo, orderTimeoutDelayMs);
    }
}
