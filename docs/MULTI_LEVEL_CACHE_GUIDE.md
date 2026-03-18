# Multi-Level Caching Guide

## Table of Contents
1. [Principles of Spring Cache Annotation System](#principles-of-spring-cache-annotation-system)

Spring Cache provides an abstraction layer for declarative caching. It works by intercepting method calls using AOP (Aspect-Oriented Programming). When a method annotated with a caching annotation is called:
1. Spring generates a proxy around the target bean.
2. The proxy intercepts the method execution.
3. It checks if the requested data is already present in the cache backend (like Redis).
4. If a cache hit occurs, the cached value is returned directly without executing the actual method.
5. If a cache miss occurs, the actual method is executed, and its return value is stored in the cache before being returned to the caller.

This decoupling allows developers to easily apply caching to business logic without tangling the code with explicit cache management API calls.

## How TTLs are Handled via `RedisCacheManager`

The `RedisCacheManager` integrates with our system's backend (Redis) to store and expire cache entries. Since different types of data change at different frequencies, having a uniform Time-To-Live (TTL) might result in stale data or memory bloating.

We handle variable TTLs by customizing the `RedisCacheManager` in `CacheConfig.java`:
- **Default TTL:** We define a base `RedisCacheConfiguration` with a standard expiration (e.g., 1 hour).
- **Specific TTLs:** We inject specific `RedisCacheConfiguration` objects mapped by the cache name string. For instance:
    - `"product:detail"` is mapped to 30 minutes.
    - `"category:tree"` is mapped to 1 hour.
    - `"product:hot"` is mapped to 5 minutes.
    - `"user:info"` is mapped to 15 minutes.
- When Spring Cache issues a command to save data under `"product:detail"`, `RedisCacheManager` detects the corresponding override and sets the TTL to exactly 30 minutes for those keys.

## Why Caching Solves the Performance Issue

1. **Reduced Database Load:** Complex reads, such as category trees, hot product aggregations, or computing product detail configurations (SKUs, images), demand massive database overhead and CPU cycles. Caching serves the result directly from memory, drastically reducing I/O and query costs.
2. **Reduced Latency:** Redis reads are almost instantaneous (sub-millisecond) compared to database queries and object parsing/mapping in Java.
3. **Throttling Bursts:** The cache shields the backend from traffic spikes, minimizing the chance of server overload and preventing the database from becoming a bottleneck during high-traffic intervals (like sales or promotional events).

## When to Use Which Annotations

### `@Cacheable`
- **When to use:** Use on methods that fetch data (read-only or predominantly read operations), where the results are relatively stable over time.
- **Purpose:** Checks the cache first. On hit, returns cached data. On miss, runs the method and populates the cache.
- **Example:** `getProductDetail`, `getCategoryTree`, `getHotProducts`.

### `@CacheEvict`
- **When to use:** Use on methods that perform modifications (create, update, delete) to data that is being cached.
- **Purpose:** Invalidates stale cache data ensuring that the subsequent read operation gets the most up-to-date data from the database.
- **Example:** `updateProduct`, `deleteProduct`, `createCategory`, `updateProfile`. Use `allEntries = true` if modifying a collection or a tree (e.g., categories).

### `@CachePut`
- **When to use:** Rarely, but often for targeted updates where the method always returns the updated object.
- **Purpose:** Unlike `@Cacheable`, `@CachePut` *always* executes the method and then places the result into the cache. This forces a cache update without sacrificing method execution.
- **Example:** Directly caching an updated configuration object post-save.
## Caching Integration Testing
It is highly recommended to establish robust integration testing when working with Spring Cache to guarantee data consistency and TTL accuracy without running the entire application manually.

The project contains a powerful test \SpringCacheIntegrationTest.java\ that demonstrates how to write a reliable proxy/cache test:
1. **Mock or Prepare Data:** Always execute database inserts or mock return values (using Mockito's \@SpyBean\ or \@MockBean\) before asserting against cache logic.
2. **Verify Execution Flow:** To truly ensure \@Cacheable\ behaves correctly (returning values from Redis directly upon a warm hit without interacting with the DB), verify invocation counts on your DAOs or Mappers. Ex: \Mockito.verify(mapper, never()).selectById(anyLong());\
3. **Verify Configuration Validity:** Validate whether the backend properly received the keys and TTL limits by asserting values directly via \RedisTemplate.getExpire(key)\.
4. **Mock Interceptors Effectively:** Ensure that proxy interceptors inherently tied to Spring's architecture (like Security interceptors using thread-local constructs such as \SecurityContextHolder\) are adequately mocked during cache invalidation (\@CacheEvict\) testing.

