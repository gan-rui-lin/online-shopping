# Performance Optimization Guide (1.1 - 1.3)

This document is written based on the current implementation. It covers the completed work for:

- 1.1 Multi-level caching
- 1.2 Database query optimization
- 1.3 Batch operations and stock optimization

All descriptions below reference actual code paths and runtime behavior.

---

## 1.1 Multi-Level Caching

### Principles

1. Cache hot read paths to reduce database load and latency.
2. Use Redis as the shared cache and Caffeine as local JVM cache for static dictionaries.
3. Add TTL jitter to avoid synchronized cache expiry (cache avalanche).
4. Use cache preheat and periodic refresh for hot datasets.
5. Block cache penetration with a Bloom filter for product detail lookups.

### Implementation Details

#### Spring Cache and TTL configuration

- Enabled via `@EnableCaching` and a custom `RedisCacheManager`.
- Default TTL is 1 hour; per-cache overrides are configured in `CacheConfig`.
- TTL jitter is added in `CustomRedisCacheManager` to spread expirations.

Configured cache names and TTLs:

- `product:detail` -> 30 minutes
- `category:tree` -> 1 hour
- `product:hot` -> 5 minutes
- `user:info` -> 15 minutes

#### Category tree cache

- `CategoryService.getCategoryTree()` uses `@Cacheable("category:tree")`.
- `CategoryService.createCategory()` uses `@CacheEvict(allEntries = true)` to invalidate the tree.

#### Product detail cache with Bloom filter

- `ProductService.getProductDetail()` uses `@Cacheable("product:detail")`.
- A Redisson Bloom filter (`product:bloom:filter`) is initialized at startup and filled with existing product IDs.
- If a product ID is not in the Bloom filter, the service returns `Product not found` immediately to avoid DB hits.
- Any product create operation adds the new ID into the Bloom filter.

#### Hot product cache and preheat

- `RecommendService.getHotProducts()` is cached by `@Cacheable("product:hot")`.
- A Redis ZSet (`hot:products`) stores hot ranking scores.
- `CachePreheatTask` runs on application startup to populate the hot list.
- `CacheRefreshTask` refreshes the hot list every 5 minutes.

#### Local JVM cache (Caffeine)

- `CaffeineConfig` defines a `dictCache` with 1 hour expiry and max size 1000.
- `DictService` reads dictionary data via `dictCache.get(...)`, reducing Redis/DB calls for static values.

---

## 1.2 Database Query Optimization

### Principles

1. Eliminate N+1 queries by batching related entity loads.
2. Use composite indexes to optimize common filters and sorting.
3. Detect slow SQL via a MyBatis interceptor.

### Implementation Details

#### N+1 query elimination

- `OrderService.getOrderList()` and `getMerchantOrders()` batch-load order items with a single `IN` query and group by `orderId`.
- `ProductService.searchProducts()` batch-loads shop names for the product list.
- `CartService.getCartList()` batch-loads SKU and SPU data and builds in-memory maps for O(1) joins.

#### Composite indexes

The SQL script `src/main/resources/db/optimize/indexes.sql` adds:

- `order_info (user_id, order_status, create_time)`
- `product_spu (status, audit_status, sales_count)`
- `cart_item (user_id, sku_id, checked)`

These indexes align with the query patterns used in list endpoints and cart checks.

#### Slow query detection

- `SlowQueryInterceptor` logs any query or update that exceeds 100 ms.
- The interceptor records method ID and normalized SQL text for faster diagnostics.

---

## 1.3 Batch Operations and Stock Optimization

### Principles

1. Batch insert and update reduce per-row overhead and JDBC round trips.
2. Redis pre-lock provides fast stock validation under concurrency.
3. The database remains the source of truth via optimistic locking.
4. Failure paths must release any pre-locked inventory to avoid dead stock.

### Implementation Details

#### Cart batch add

- `CartService.addItemsBatch(...)` merges duplicate SKU entries and validates stock in bulk.
- Existing items are updated with `updateBatchById`; new items use `saveBatch`.
- Cart cache (`cart:user:{userId}`) is invalidated after batch write.

#### Order item batch insert

- `OrderService.submitOrder(...)` collects order items per shop and inserts in a single batch via `OrderItemBatchService.saveBatch(...)`.

#### Redis stock pre-lock (Lua)

Redis keys:

- `stock:sku:{skuId}` (Hash)
  - field `stock` : available stock
  - field `lock`  : locked stock

Scripts in `src/main/resources/scripts/`:

- `lock-stock.lua` checks all SKUs first, then atomically moves quantities from `stock` to `lock`.
- `deduct-lock.lua` reduces `lock` after payment confirmation.
- `unlock-stock.lua` returns `lock` back to `stock` on cancellation.

#### Database confirmation and rollback

- After Redis pre-lock, `ProductSkuMapper.lockStock(...)` uses optimistic locking to update `product_sku`.
- Inventory changes are written to `inventory_log`.
- If any part of the submit flow fails, the service executes `unlock-stock.lua` to roll back the Redis pre-lock.

---

## Summary

The 1.1 to 1.3 optimizations combine cache layering, query batching, and fast stock pre-locking to reduce latency and improve concurrency safety while keeping the database as the final authority for inventory state.
