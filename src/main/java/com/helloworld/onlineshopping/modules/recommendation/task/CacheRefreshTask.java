package com.helloworld.onlineshopping.modules.recommendation.task;

import com.helloworld.onlineshopping.modules.recommendation.service.CoPurchaseService;
import com.helloworld.onlineshopping.modules.recommendation.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Nightly full cache refresh: hot products + co-purchase matrix.
 * Runs once every 12 hours to keep recommendation data fresh.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshTask {

    private final RecommendService recommendService;
    private final CoPurchaseService coPurchaseService;

    @Scheduled(fixedRate = 12 * 3600 * 1000, initialDelay = 120000)
    public void refreshAllRecommendationData() {
        log.info("--- Scheduled task: full recommendation data refresh ---");
        try {
            recommendService.refreshHotProducts();
            coPurchaseService.rebuildCoPurchaseMatrix();
            log.info("--- Full recommendation data refresh complete ---");
        } catch (Exception e) {
            log.error("Recommendation data refresh failed", e);
        }
    }
}
