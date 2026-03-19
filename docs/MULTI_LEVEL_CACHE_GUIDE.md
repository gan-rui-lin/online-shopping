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

## Redis Advanced Optimizations

Our project goes beyond standard caching annotations to handle extreme concurrency and prevent common cache-related technical pitfalls (Cache Avalanche, Cache Penetration, etc.).

### 1. Handling Cache Avalanche 

A Cache Avalanche occurs when a large amount of cache keys expire at the exact same moment. This forces all subsequent requests for these keys to query the database simultaneously, potentially causing a system crash.

- **Solution :TTL Random Jitter :**
  In the CustomRedisCacheManager.java, we override createRedisCache to add a random jitter (60-300 seconds) to the configured TTL of cached items. This breaks up uniform expirations so keys expire at different times.

### 2. Handling Cache Penetration 

Cache Penetration is when clients query for keys that do NOT exist in the Cache AND the Database. Without protective measures, these queries would hit the database repeatedly.

- **Solution A :Preventative Caching of Null:** We removed .disableCachingNullValues() in CacheConfig to allow the cache to store
  ull results for non-existent queries.
- **Solution B :Redisson Bloom Filters:**
  Using
  edisson-spring-boot-starter, we implemented a Bloom Filter in ProductService.
  - During startup, initBloomFilter pre-hashes valid product IDs into a memory-efficient filter inside Redis.
  - New product creations automatically register their ID to the existing Bloom Filter.
  - Any product lookup operations (getProductDetail) check the filter first via !productBloomFilter.contains(spuId). If blocked by the Bloom filter, the request is immediately rejected without touching the DB.

### 3. Cache Warming / Preheating 

When the server restarts or caches are cleared, high traffic can hit the database directly (Cache Breakdown).

- **Solution :ApplicationRunner Scheduled Preheating:**
  We implemented CachePreheatTask.java utilizing Spring's ApplicationRunner. Upon successful boot, it automatically queries hot products and seeds the initial Redis mappings. Additionally, CacheRefreshTask runs on a schedule to repeatedly refresh these values in the background.

## Local Caching Optimizations

To further reduce network overhead between our application and Redis, we introduced **Caffeine** for Local JVM Caching.

### 1. Handling Static & Dictionary Data

For highly accessed, read-heavy, and completely static configurations (like `ORDER_STATUS`, `SYS_CONFIG`), reading from Redis still incurs network latency and deserialization costs. 

- **Solution :Local Caffeine Cache:**
  We introduced `CaffeineConfig.java` providing a localized memory cache (`dictCache`).
  - This local cache holds up to 1000 items and automatically expires them an hour after the last write.
  - The `DictService.java` is designed to intercept mapping fetches. It loads static configurations directly from local JVM memory before checking the database.
  - Integration tests (`CaffeineCacheTest.java`) mathematically verify that instances retrieved are identical address spaces (`==`), confirming that network operations and object reallocations are eliminated.
---

# Database Query Optimizations

While caching significantly reduces the load on the database, optimizing the underlying database queries is still essential for operations that miss the cache, write heavily, or involve complex relational data. The following database optimizations have been implemented to ensure robust performance:

## 1. Resolving N+1 Query Problems
The N+1 query problem occurs when an application executes 1 query to retrieve a list of $N$ entities, and then performs $N$ additional queries to fetch a related entity for each. This drastically degrades performance.

**Solution: In-Memory Map Joins via `selectBatchIds`**
- We refactored services (`OrderService`, `ProductService`, `CartService`) by extracting a list or set of the target `ID`s using Java Streams.
- Next, we use MyBatis-Plus's `selectBatchIds(ids)` or `.in(id, ids)` conditional queries to load all related entities in a single batch query.
- Finally, Java Stream `Collectors.groupingBy()` or `Collectors.toMap()` is used to quickly associate the fetched entities with the original parents in memory ($O(1)$ lookup time).
- This strictly bounds the maximum number of queries to 2 (one for the parents, one for the relationships) regardless of the dataset size.

## 2. Composite Indexes (`indexes.sql`)
Indexing creates B+Tree data structures that allow MySQL to locate records without performing a full table scan. Composite indexes cover multiple columns to optimize filtering and sorting simultaneously.

- **`order_entity`** `(user_id, order_status, create_time)`: Optimizes frequent Buyer-side queries where they view their orders grouped by `order_status` and sorted by `create_time` descending.
- **`product_spu`** `(status, audit_status, sales_count)`: Powers the core marketplace catalog. Automatically filters products to show only active (`status = 1`) and approved (`audit_status = 1`) items while allowing immediate reverse sorting by `sales_count`.
- **`cart_item`** `(user_id, sku_id, checked)`: Accelerates shopping cart lookups, duplicate-check logic, and fast fetching of `checked` items during the checkout and calculation phase. 

*Note: The script `src/main/resources/db/optimize/indexes.sql` must be successfully executed in MySQL for these physical optimizations to take effect.*

## 3. Slow Query Interceptor Integration
Monitoring query performance is key for preemptive optimization. Not all queries maintain $O(1)$ or $O(\log n)$ performance as tables grow to millions of rows.

**Solution: MyBatis Physical `Interceptor`**
- We added `SlowQueryInterceptor.java` directly into the MyBatis-Plus plugin chain.
- This plugin intercepts the foundational `Executor.class` methods (`query` and `update`).
- It captures the `BoundSql` and times the exact execution block `invocation.proceed()`.
- If the milliseconds elapsed exceed a specific risk threshold (`100ms`), the exact parameterized SQL string and the time taken are heavily logged as `WARN`. This makes it immediately obvious during APM monitoring which DB operations are bottlenecking the application.