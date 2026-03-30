-- =============================================
-- Online Shopping Platform - Database Schema
-- =============================================

CREATE DATABASE IF NOT EXISTS online_shopping DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE online_shopping;

-- =============================================
-- 1. User & Permission Tables
-- =============================================

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64),
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(128),
    avatar_url VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0disabled 1enabled',
    user_type TINYINT NOT NULL DEFAULT 1 COMMENT '1buyer 2merchant 3admin',
    last_login_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_phone (phone),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user table';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL UNIQUE,
    role_name VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='role table';

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    perm_code VARCHAR(128) NOT NULL UNIQUE,
    perm_name VARCHAR(64) NOT NULL,
    perm_type TINYINT NOT NULL COMMENT '1menu 2button 3api',
    path VARCHAR(255),
    method VARCHAR(10),
    parent_id BIGINT DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='permission table';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user role relation';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='role permission relation';

-- =============================================
-- 2. Merchant Tables
-- =============================================

CREATE TABLE IF NOT EXISTS merchant_apply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    shop_name VARCHAR(128) NOT NULL,
    business_license_no VARCHAR(128),
    contact_name VARCHAR(64),
    contact_phone VARCHAR(20),
    apply_status TINYINT NOT NULL DEFAULT 0 COMMENT '0pending 1approved 2rejected',
    remark VARCHAR(255),
    audit_by BIGINT,
    audit_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_apply_status (apply_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='merchant application';

CREATE TABLE IF NOT EXISTS merchant_shop (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    shop_name VARCHAR(128) NOT NULL,
    shop_logo VARCHAR(255),
    shop_desc VARCHAR(500),
    shop_status TINYINT NOT NULL DEFAULT 1 COMMENT '0disabled 1enabled',
    score DECIMAL(3,2) DEFAULT 5.00,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='merchant shop';

-- =============================================
-- 3. Product Tables
-- =============================================

CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    category_name VARCHAR(64) NOT NULL,
    level TINYINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    icon VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product category';

CREATE TABLE IF NOT EXISTS product_spu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_id BIGINT NOT NULL,
    category_id BIGINT,
    brand_name VARCHAR(64),
    title VARCHAR(255) NOT NULL,
    sub_title VARCHAR(255),
    main_image VARCHAR(255) COMMENT 'Public image URL, e.g. /cdn/images/xxx.jpg',
    detail_text TEXT,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0draft 1on_shelf 2off_shelf',
    audit_status TINYINT NOT NULL DEFAULT 0 COMMENT '0pending 1approved 2rejected',
    min_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    max_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    sales_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    favorite_count INT NOT NULL DEFAULT 0,
    browse_count INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_shop_id (shop_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_audit_status (audit_status),
    INDEX idx_sales_count (sales_count),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product SPU';

CREATE TABLE IF NOT EXISTS product_sku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    spu_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL UNIQUE,
    sku_name VARCHAR(255),
    sale_price DECIMAL(10,2) NOT NULL,
    origin_price DECIMAL(10,2) DEFAULT 0.00,
    stock INT NOT NULL DEFAULT 0,
    lock_stock INT NOT NULL DEFAULT 0,
    warning_stock INT NOT NULL DEFAULT 10,
    image_url VARCHAR(255) COMMENT 'Public image URL, e.g. /cdn/images/xxx.jpg',
    spec_json VARCHAR(500) COMMENT 'JSON spec e.g. {"color":"red","size":"XL"}',
    status TINYINT NOT NULL DEFAULT 1,
    version INT NOT NULL DEFAULT 0 COMMENT 'optimistic lock version',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_spu_id (spu_id),
    INDEX idx_sku_code (sku_code),
    INDEX idx_sale_price (sale_price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product SKU';

CREATE TABLE IF NOT EXISTS product_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    spu_id BIGINT NOT NULL,
    sku_id BIGINT DEFAULT NULL,
    image_url VARCHAR(255) NOT NULL COMMENT 'Public image URL, e.g. /cdn/images/xxx.jpg',
    image_type TINYINT NOT NULL DEFAULT 1 COMMENT '1spu_main 2spu_detail 3sku',
    sort_order INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_spu_id (spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product image';

-- =============================================
-- 4. Cart & Address Tables
-- =============================================

CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    checked TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_sku (user_id, sku_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='cart item';

CREATE TABLE IF NOT EXISTS user_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(64) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(64) NOT NULL,
    city VARCHAR(64) NOT NULL,
    district VARCHAR(64) NOT NULL,
    detail_address VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20),
    is_default TINYINT NOT NULL DEFAULT 0,
    tag_name VARCHAR(20),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user address';

-- =============================================
-- 5. Order & Payment Tables
-- =============================================

CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    pay_amount DECIMAL(10,2) NOT NULL,
    freight_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    order_status TINYINT NOT NULL DEFAULT 0 COMMENT '0unpaid 1to_ship 2to_receive 3completed 4cancelled 5refunding 6refunded',
    pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '0unpaid 1paid 2refunded',
    source_type TINYINT NOT NULL DEFAULT 1 COMMENT '1normal 2agent 3plan',
    receiver_name VARCHAR(64) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    remark VARCHAR(255),
    pay_time DATETIME,
    delivery_time DATETIME,
    finish_time DATETIME,
    cancel_time DATETIME,
    cancel_reason VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_shop_id (shop_id),
    INDEX idx_order_status (order_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='order info';

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    spu_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    product_title VARCHAR(255) NOT NULL,
    sku_name VARCHAR(255),
    sku_spec_json VARCHAR(500),
    product_image VARCHAR(255),
    sale_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    review_status TINYINT NOT NULL DEFAULT 0 COMMENT '0not_reviewed 1reviewed',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='order item';

CREATE TABLE IF NOT EXISTS order_operate_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    before_status TINYINT,
    after_status TINYINT,
    operator_id BIGINT,
    operator_role VARCHAR(32),
    operate_type VARCHAR(64) NOT NULL,
    remark VARCHAR(255),
    operate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='order operation log';

CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    pay_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    pay_amount DECIMAL(10,2) NOT NULL,
    pay_method TINYINT NOT NULL DEFAULT 1 COMMENT '1simulated_pay',
    pay_status TINYINT NOT NULL COMMENT '0pending 1success 2failed',
    third_trade_no VARCHAR(128),
    pay_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='payment record';

CREATE TABLE IF NOT EXISTS inventory_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku_id BIGINT NOT NULL,
    order_no VARCHAR(64),
    change_count INT NOT NULL,
    before_stock INT NOT NULL,
    after_stock INT NOT NULL,
    operate_type VARCHAR(32) NOT NULL COMMENT 'LOCK/UNLOCK/DEDUCT/RETURN',
    remark VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sku_id (sku_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='inventory change log';

-- =============================================
-- 6. Review Tables
-- =============================================

CREATE TABLE IF NOT EXISTS product_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_item_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    spu_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    score TINYINT NOT NULL,
    content VARCHAR(1000),
    image_urls VARCHAR(2000),
    anonymous_flag TINYINT NOT NULL DEFAULT 0,
    reply_content VARCHAR(1000),
    reply_time DATETIME,
    review_status TINYINT NOT NULL DEFAULT 1 COMMENT '0hidden 1visible',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_spu_id (spu_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product review';

-- =============================================
-- 7. User Behavior Tables
-- =============================================

CREATE TABLE IF NOT EXISTS user_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    spu_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_spu (user_id, spu_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user favorite';

CREATE TABLE IF NOT EXISTS user_browse_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    spu_id BIGINT NOT NULL,
    browse_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_browse_time (browse_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user browse history';

-- =============================================
-- 8. AI / Intelligence Tables
-- =============================================

CREATE TABLE IF NOT EXISTS product_knowledge_doc (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    spu_id BIGINT NOT NULL,
    title VARCHAR(255),
    content TEXT NOT NULL,
    source_type VARCHAR(32) COMMENT 'PRODUCT_DESC/FAQ/AFTER_SALE',
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_spu_id (spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG knowledge document';

CREATE TABLE IF NOT EXISTS ai_chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    session_type VARCHAR(32) NOT NULL COMMENT 'RAG/AGENT/PLAN',
    spu_id BIGINT,
    title VARCHAR(128),
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI chat session';

CREATE TABLE IF NOT EXISTS ai_chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL COMMENT 'user/assistant/system',
    content TEXT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI chat message';

CREATE TABLE IF NOT EXISTS shopping_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plan_name VARCHAR(128) NOT NULL,
    trigger_time DATETIME,
    plan_status TINYINT NOT NULL DEFAULT 0 COMMENT '0created 1reminded 2executed 3cancelled',
    budget_amount DECIMAL(10,2),
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_trigger_time (trigger_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='shopping plan';

CREATE TABLE IF NOT EXISTS shopping_plan_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    keyword VARCHAR(128),
    category_id BIGINT,
    expected_price_min DECIMAL(10,2),
    expected_price_max DECIMAL(10,2),
    quantity INT NOT NULL DEFAULT 1,
    matched_spu_id BIGINT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='shopping plan item';

CREATE TABLE IF NOT EXISTS agent_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    task_type VARCHAR(32) NOT NULL COMMENT 'SHOPPING/RECOMMEND/COPYWRITING',
    user_prompt TEXT NOT NULL,
    task_status TINYINT NOT NULL DEFAULT 0 COMMENT '0created 1running 2completed 3failed',
    result_json TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='agent task';

-- =============================================
-- 9. Init Data
-- =============================================

-- Default roles
INSERT IGNORE INTO sys_role (id, role_code, role_name) VALUES
(1, 'ROLE_BUYER', 'Buyer'),
(2, 'ROLE_MERCHANT', 'Merchant'),
(3, 'ROLE_ADMIN', 'Administrator');

-- Default admin user (password: admin123, BCrypt encoded)
INSERT IGNORE INTO sys_user (id, username, password, nickname, user_type, status) VALUES
(1, 'admin', '$2a$10$77fqFla9dMpEi4DR1J8WBO6qaJKrR8WChf8I5jwugvpkVseMI1yBe', 'System Admin', 3, 1);

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 3);

-- Default categories
INSERT IGNORE INTO product_category (id, parent_id, category_name, level, sort_order) VALUES
(1, 0, 'Electronics', 1, 1),
(2, 0, 'Clothing', 1, 2),
(3, 0, 'Home & Living', 1, 3),
(4, 0, 'Books', 1, 4),
(5, 0, 'Food & Beverage', 1, 5),
(6, 1, 'Phones', 2, 1),
(7, 1, 'Laptops', 2, 2),
(8, 1, 'Accessories', 2, 3),
(9, 2, 'Men', 2, 1),
(10, 2, 'Women', 2, 2);
