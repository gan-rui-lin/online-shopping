package com.helloworld.onlineshopping.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // Default TTL 1 hour
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        configMap.put("product:detail", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        configMap.put("category:tree", defaultConfig.entryTtl(Duration.ofHours(1)));
        configMap.put("product:hot", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        configMap.put("user:info", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        configMap.put("ai:sellingPoints", defaultConfig.entryTtl(Duration.ofMinutes(20)));
        configMap.put("ai:reviewSummary", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configMap.put("ai:productEvaluation", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        org.springframework.data.redis.cache.RedisCacheWriter redisCacheWriter = org.springframework.data.redis.cache.RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
        return new CustomRedisCacheManager(redisCacheWriter, defaultConfig, configMap);
    }
}
