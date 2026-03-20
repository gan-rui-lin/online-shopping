package com.helloworld.onlineshopping.modules.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderAsyncService {

    @Async("businessAsyncExecutor")
    public void recordOrderStatusLog(String orderNo, String action, String operatorRole) {
        log.info("Async order operation log: orderNo={}, action={}, role={}", orderNo, action, operatorRole);
    }

    @Async("businessAsyncExecutor")
    public void updateSalesStat(Long spuId, Integer delta) {
        log.info("Async sales stat update: spuId={}, delta={}", spuId, delta);
    }
}
