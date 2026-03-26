package com.helloworld.onlineshopping.modules.recommendation.scheduler;

import com.helloworld.onlineshopping.modules.recommendation.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotProductScheduler {
    private final RecommendService recommendService;

    @Scheduled(fixedRate = 300000)
    public void refresh() {
        log.info("--- Scheduled task: refreshing hot products ---");
        try {
            recommendService.refreshHotProducts();
            log.info("--- Hot products refresh complete ---");
        } catch (Exception e) {
            log.error("Hot products refresh failed", e);
        }
    }
}
