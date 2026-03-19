-- -------------------------------------------------------------
-- 优化第1.2阶段：添加相关表的复合索引
-- 目的：优化查询性能，减少扫描记录数，避免由于数据量上升导致的慢查询
-- -------------------------------------------------------------

-- 1. order_info: (user_id, order_status, create_time)
-- 场景：用户订单列表查询（买家端最频繁查询：按状态筛选和按创建时间倒序）
ALTER TABLE `order_info` ADD INDEX `idx_user_status_time` (`user_id`, `order_status`, `create_time`);

-- 2. product_spu: (status, audit_status, sales_count)
-- 场景：客户端C端商品列表查询（只展示已上架+审核通过，并按销量排序）
ALTER TABLE `product_spu` ADD INDEX `idx_status_audit_sales` (`status`, `audit_status`, `sales_count`);

-- 3. cart_item: (user_id, sku_id, checked)
-- 场景：购物车查询（根据用户查购物车，或验证单个商品是否在购物车，或结算时过滤checked商品）
ALTER TABLE `cart_item` ADD INDEX `idx_user_sku_checked` (`user_id`, `sku_id`, `checked`);
