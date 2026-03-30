package com.helloworld.onlineshopping.modules.recommendation.event;

import com.helloworld.onlineshopping.common.mq.RabbitMQConfig;
import com.helloworld.onlineshopping.modules.recommendation.service.UserInterestProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final UserInterestProfileService interestProfileService;

    /**
     * Publish event to RabbitMQ. If RabbitMQ is unavailable, falls back to
     * synchronous profile invalidation so recommendations still update.
     */
    public void publish(RecommendEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.RECOMMENDATION_EVENT_EXCHANGE,
                RabbitMQConfig.RECOMMENDATION_EVENT_ROUTING_KEY,
                event);
            log.debug("Published recommendation event: userId={}, spuId={}, type={}",
                event.getUserId(), event.getSpuId(), event.getEventType());
        } catch (Exception e) {
            log.debug("RabbitMQ unavailable, falling back to sync invalidation for userId={}",
                event.getUserId());
            try {
                interestProfileService.invalidateProfile(event.getUserId());
            } catch (Exception ex) {
                log.debug("Sync invalidation also failed: {}", ex.getMessage());
            }
        }
    }
}
