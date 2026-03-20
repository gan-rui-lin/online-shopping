package com.helloworld.onlineshopping.common.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EVENT_EXCHANGE = "order.event.exchange";
    public static final String ORDER_TIMEOUT_DELAY_QUEUE = "order.timeout.delay.queue";
    public static final String ORDER_TIMEOUT_QUEUE = "order.timeout.queue";
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

    public static final String ORDER_TIMEOUT_DLX_EXCHANGE = "order.timeout.dlx.exchange";
    public static final String ORDER_TIMEOUT_DLX_ROUTING_KEY = "order.timeout.dlx";

    public static final String STOCK_EVENT_EXCHANGE = "stock.event.exchange";
    public static final String STOCK_EVENT_QUEUE = "stock.event.queue";
    public static final String STOCK_EVENT_ROUTING_KEY = "stock.event";

    public static final String RECOMMENDATION_EVENT_EXCHANGE = "recommendation.event.exchange";
    public static final String RECOMMENDATION_EVENT_QUEUE = "recommendation.event.queue";
    public static final String RECOMMENDATION_EVENT_ROUTING_KEY = "recommendation.sync";

    @Bean
    public DirectExchange orderEventExchange() {
        return new DirectExchange(ORDER_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange orderTimeoutDlxExchange() {
        return new DirectExchange(ORDER_TIMEOUT_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderTimeoutDelayQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_DELAY_QUEUE)
            .deadLetterExchange(ORDER_TIMEOUT_DLX_EXCHANGE)
            .deadLetterRoutingKey(ORDER_TIMEOUT_DLX_ROUTING_KEY)
            .build();
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(ORDER_TIMEOUT_QUEUE).build();
    }

    @Bean
    public Binding orderTimeoutDelayBinding() {
        return BindingBuilder.bind(orderTimeoutDelayQueue())
            .to(orderEventExchange())
            .with(ORDER_TIMEOUT_ROUTING_KEY);
    }

    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue())
            .to(orderTimeoutDlxExchange())
            .with(ORDER_TIMEOUT_DLX_ROUTING_KEY);
    }

    @Bean
    public DirectExchange stockEventExchange() {
        return new DirectExchange(STOCK_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue stockEventQueue() {
        return QueueBuilder.durable(STOCK_EVENT_QUEUE).build();
    }

    @Bean
    public Binding stockEventBinding() {
        return BindingBuilder.bind(stockEventQueue())
            .to(stockEventExchange())
            .with(STOCK_EVENT_ROUTING_KEY);
    }

    @Bean
    public DirectExchange recommendationEventExchange() {
        return new DirectExchange(RECOMMENDATION_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue recommendationEventQueue() {
        return QueueBuilder.durable(RECOMMENDATION_EVENT_QUEUE).build();
    }

    @Bean
    public Binding recommendationEventBinding() {
        return BindingBuilder.bind(recommendationEventQueue())
            .to(recommendationEventExchange())
            .with(RECOMMENDATION_EVENT_ROUTING_KEY);
    }
}
