package com.helloworld.onlineshopping.modules.recommendation.event;

import com.helloworld.onlineshopping.common.mq.RabbitMQConfig;
import com.helloworld.onlineshopping.modules.recommendation.service.UserInterestProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumes recommendation events from RabbitMQ and updates the user
 * interest profile cache in real-time. Only active when RabbitMQ is enabled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RecommendEventConsumer {

    private final UserInterestProfileService interestProfileService;

    @RabbitListener(queues = RabbitMQConfig.RECOMMENDATION_EVENT_QUEUE)
    public void handleEvent(RecommendEvent event) {
        try {
            log.debug("Received recommendation event: userId={}, spuId={}, type={}",
                event.getUserId(), event.getSpuId(), event.getEventType());

            interestProfileService.invalidateProfile(event.getUserId());

        } catch (Exception e) {
            log.error("Error processing recommendation event: {}", e.getMessage(), e);
        }
    }
}
