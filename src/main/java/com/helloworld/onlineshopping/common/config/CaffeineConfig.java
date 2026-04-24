package com.helloworld.onlineshopping.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {

    /**
     * Dictionary and Configuration Cache.
     * Use case: Data that rarely changes (like dictionary tables or basic system configurations).
     */
    @Bean("dictCache")
    public Cache<String, Object> dictCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    /**
     * Short-lived cache for repeated RAG questions in the same context.
     */
    @Bean("intelligenceRagCache")
    public Cache<String, Object> intelligenceRagCache() {
        return Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(2000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }
}
