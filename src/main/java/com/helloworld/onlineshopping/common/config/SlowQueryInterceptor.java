package com.helloworld.onlineshopping.common.config;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class SlowQueryInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SlowQueryInterceptor.class);
    
    // Threshold for slow queries in milliseconds
    private static final long SLOW_QUERY_THRESHOLD_MS = 100;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = invocation.proceed();
        long time = System.currentTimeMillis() - start;

        if (time > SLOW_QUERY_THRESHOLD_MS) {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args[1];
            BoundSql boundSql = ms.getBoundSql(parameter);
            String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
            
            log.warn("=== SLOW SQL DETECTED ===");
            log.warn("Execution time: {} ms", time);
            log.warn("Method: {}", ms.getId());
            log.warn("SQL: {}", sql);
            log.warn("=========================");
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // Can be configured from properties
    }
}
