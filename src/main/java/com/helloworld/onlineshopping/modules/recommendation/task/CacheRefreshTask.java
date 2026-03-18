package com.helloworld.onlineshopping.modules.recommendation.task;

import com.helloworld.onlineshopping.modules.recommendation.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshTask {

    private final RecommendService recommendService;

    // 定时刷新热门商品，每5分钟执行一次
    @Scheduled(fixedRate = 300000)
    public void refreshHotData() {
        log.info("--- 定时任务：开始刷新热点缓存数据 ---");
        try {
            recommendService.refreshHotProducts();
            log.info("--- 定时任务：热点缓存数据刷新完成 ---");
        } catch (Exception e) {
            log.error("热点数据刷新失败", e);
        }
    }
}
