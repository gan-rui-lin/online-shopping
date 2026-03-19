package com.helloworld.onlineshopping.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private String port;

    @Value("${spring.data.redis.password:#{null}}")
    private String password;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        
        config.useSingleServer().setAddress(address)
              .setPingConnectionInterval(1000); // 增加心跳
        
        // 只有当密码确实配置了且不为空时，才设置密码，防止产生 ERR Client sent AUTH, but no password is set 错误
        if (password != null && !password.trim().isEmpty() && !password.equals("${REDIS_PASSWORD:}")) {
            config.useSingleServer().setPassword(password);
        }
        
        return Redisson.create(config);
    }
}
