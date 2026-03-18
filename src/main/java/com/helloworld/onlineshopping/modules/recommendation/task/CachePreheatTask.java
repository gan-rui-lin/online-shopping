package com.helloworld.onlineshopping.modules.recommendation.task;

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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("--- 开始执行缓存预热 ---");
        try {
            recommendService.refreshHotProducts();
            log.info("--- 缓存预热执行成功 ---");
        } catch (Exception e) {
            log.error("缓存预热执行失败", e);
        }
    }
}
