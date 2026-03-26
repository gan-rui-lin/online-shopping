package com.helloworld.onlineshopping.modules.recommendation.task;

import com.helloworld.onlineshopping.modules.recommendation.service.CoPurchaseService;
import com.helloworld.onlineshopping.modules.recommendation.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CachePreheatTask implements ApplicationRunner {

    private final RecommendService recommendService;
    private final CoPurchaseService coPurchaseService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("--- Starting recommendation cache preheat ---");
        try {
            recommendService.refreshHotProducts();
            log.info("--- Hot products preheat complete ---");
        } catch (Exception e) {
            log.error("Hot products preheat failed", e);
        }
        try {
            coPurchaseService.rebuildCoPurchaseMatrix();
            log.info("--- Co-purchase matrix preheat complete ---");
        } catch (Exception e) {
            log.error("Co-purchase matrix preheat failed", e);
        }
    }
}
