package com.helloworld.onlineshopping.modules.recommendation.scheduler;

import com.helloworld.onlineshopping.modules.recommendation.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotProductScheduler {
    private final RecommendService recommendService;

    @Scheduled(fixedRate = 600000)
    public void refresh() {
        recommendService.refreshHotProducts();
    }
}
