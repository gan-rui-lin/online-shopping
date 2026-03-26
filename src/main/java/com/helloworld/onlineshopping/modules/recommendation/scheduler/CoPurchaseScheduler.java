package com.helloworld.onlineshopping.modules.recommendation.scheduler;

import com.helloworld.onlineshopping.modules.recommendation.service.CoPurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Rebuilds the co-purchase matrix periodically.
 * Runs every 6 hours to keep collaborative filtering data fresh.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoPurchaseScheduler {

    private final CoPurchaseService coPurchaseService;

    @Scheduled(fixedRate = 6 * 3600 * 1000, initialDelay = 60000)
    public void rebuildMatrix() {
        log.info("--- Scheduled task: rebuilding co-purchase matrix ---");
        try {
            coPurchaseService.rebuildCoPurchaseMatrix();
            log.info("--- Co-purchase matrix rebuild complete ---");
        } catch (Exception e) {
            log.error("Co-purchase matrix rebuild failed", e);
        }
    }
}
