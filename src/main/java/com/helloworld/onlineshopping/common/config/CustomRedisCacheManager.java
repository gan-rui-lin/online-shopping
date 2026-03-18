package com.helloworld.onlineshopping.common.config;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Map;

public class CustomRedisCacheManager extends RedisCacheManager {

    public CustomRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration, Map<String, RedisCacheConfiguration> initialCacheConfigurations) {
        super(cacheWriter, defaultCacheConfiguration, initialCacheConfigurations);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        Duration existingTtl = cacheConfig.getTtl();
        // 如果配置了具体的过期时间，则加上一个随机的抖动（Jitter），防止缓存雪崩
        if (existingTtl != null && !existingTtl.isZero()) {
            // 在原基础上随机增加 60 到 300 秒 (1 到 5 分钟) 的过期时间
            long jitterSeconds = ThreadLocalRandom.current().nextLong(60, 301);
            cacheConfig = cacheConfig.entryTtl(existingTtl.plusSeconds(jitterSeconds));
        }
        return super.createRedisCache(name, cacheConfig);
    }
}
