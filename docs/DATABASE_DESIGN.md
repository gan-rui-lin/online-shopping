# Database Design Documentation

## Overview

The Online Shopping Platform uses MySQL 8 with utf8mb4 encoding to support international characters. The database schema follows a modular design with 9 core functional areas covering user management, product catalog, order processing, and AI-powered features.

**Database Name**: `online_shopping`
**Character Set**: `utf8mb4_unicode_ci`
**Total Tables**: 27 tables across 9 modules

---

## 1. User & Permission Module

### 1.1 `sys_user` - User Account Table

Stores all platform users including buyers, merchants, and administrators.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | User unique identifier |
| `username` | VARCHAR(64) | NOT NULL, UNIQUE | Login username |
| `password` | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| `nickname` | VARCHAR(64) | | Display name |
| `phone` | VARCHAR(20) | UNIQUE | Mobile phone number |
| `email` | VARCHAR(128) | | Email address |
| `avatar_url` | VARCHAR(255) | | Profile picture URL |
| `status` | TINYINT | NOT NULL, DEFAULT 1 | Account status: 0=disabled, 1=enabled |
| `user_type` | TINYINT | NOT NULL, DEFAULT 1 | User role: 1=buyer, 2=merchant, 3=admin |
| `last_login_time` | DATETIME | | Last login timestamp |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Account creation time |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last modification time |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag: 0=active, 1=deleted |

**Indexes**:
- `idx_phone`: Fast lookup by phone number
- `idx_user_type`: Filter users by role type

**Business Rules**:
- Passwords are encrypted using BCrypt with strength 10
- Username and phone must be unique across the system
- Soft delete is used to preserve historical data

---

### 1.2 `sys_role` - Role Table

Defines system roles for RBAC (Role-Based Access Control).

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Role unique identifier |
| `role_code` | VARCHAR(64) | NOT NULL, UNIQUE | Role code (e.g., ROLE_BUYER) |
| `role_name` | VARCHAR(64) | NOT NULL | Role display name |
| `status` | TINYINT | NOT NULL, DEFAULT 1 | Role status: 0=disabled, 1=enabled |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update timestamp |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag |

**Default Roles**:
- `ROLE_BUYER` (id=1): Regular customer
- `ROLE_MERCHANT` (id=2): Shop owner
- `ROLE_ADMIN` (id=3): Platform administrator

---

### 1.3 `sys_permission` - Permission Table

Defines granular permissions for menu access, button actions, and API endpoints.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Permission unique identifier |
| `perm_code` | VARCHAR(128) | NOT NULL, UNIQUE | Permission code |
| `perm_name` | VARCHAR(64) | NOT NULL | Permission name |
| `perm_type` | TINYINT | NOT NULL | Permission type: 1=menu, 2=button, 3=api |
| `path` | VARCHAR(255) | | API path or menu route |
| `method` | VARCHAR(10) | | HTTP method (GET, POST, etc.) |
| `parent_id` | BIGINT | DEFAULT 0 | Parent permission ID (for hierarchical structure) |
| `status` | TINYINT | NOT NULL, DEFAULT 1 | Status: 0=disabled, 1=enabled |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update timestamp |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag |

---

### 1.4 `sys_user_role` - User-Role Association Table

Many-to-many relationship between users and roles.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Association unique identifier |
| `user_id` | BIGINT | NOT NULL | Foreign key to sys_user.id |
| `role_id` | BIGINT | NOT NULL | Foreign key to sys_role.id |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Association creation time |

**Indexes**:
- `uk_user_role`: Unique constraint on (user_id, role_id)
- `idx_user_id`: Fast lookup by user
- `idx_role_id`: Fast lookup by role

---

### 1.5 `sys_role_permission` - Role-Permission Association Table

Many-to-many relationship between roles and permissions.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Association unique identifier |
| `role_id` | BIGINT | NOT NULL | Foreign key to sys_role.id |
| `permission_id` | BIGINT | NOT NULL | Foreign key to sys_permission.id |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Association creation time |

**Indexes**:
- `uk_role_perm`: Unique constraint on (role_id, permission_id)

---

## 2. Merchant Module

### 2.1 `merchant_apply` - Merchant Application Table

Tracks merchant registration applications and approval workflow.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Application unique identifier |
| `user_id` | BIGINT | NOT NULL | Applicant user ID |
| `shop_name` | VARCHAR(128) | NOT NULL | Desired shop name |
| `business_license_no` | VARCHAR(128) | | Business license number |
| `contact_name` | VARCHAR(64) | | Contact person name |
| `contact_phone` | VARCHAR(20) | | Contact phone number |
| `apply_status` | TINYINT | NOT NULL, DEFAULT 0 | Status: 0=pending, 1=approved, 2=rejected |
| `remark` | VARCHAR(255) | | Application notes |
| `audit_by` | BIGINT | | Admin ID who reviewed the application |
| `audit_time` | DATETIME | | Review timestamp |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Application submission time |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Indexes**:
- `idx_user_id`: Lookup applications by user
- `idx_apply_status`: Filter by application status

**Business Flow**:
1. User submits merchant application
2. Admin reviews and approves/rejects
3. On approval, merchant_shop record is created automatically

---

### 2.2 `merchant_shop` - Merchant Shop Table

Stores merchant shop information and operating status.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Shop unique identifier |
| `user_id` | BIGINT | NOT NULL, UNIQUE | Shop owner user ID |
| `shop_name` | VARCHAR(128) | NOT NULL | Shop name |
| `shop_logo` | VARCHAR(255) | | Shop logo URL |
| `shop_desc` | VARCHAR(500) | | Shop description |
| `shop_status` | TINYINT | NOT NULL, DEFAULT 1 | Status: 0=disabled, 1=enabled |
| `score` | DECIMAL(3,2) | DEFAULT 5.00 | Shop rating (1.00-5.00) |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Shop creation time |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag |

**Indexes**:
- `idx_user_id`: Lookup shop by owner

**Business Rules**:
- One user can only own one shop (user_id UNIQUE constraint)
- Default rating is 5.00 stars

---

## 3. Product Module

### 3.1 `product_category` - Product Category Table

Hierarchical product categories with unlimited levels.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Category unique identifier |
| `parent_id` | BIGINT | NOT NULL, DEFAULT 0 | Parent category ID (0 for root) |
| `category_name` | VARCHAR(64) | NOT NULL | Category name |
| `level` | TINYINT | NOT NULL, DEFAULT 1 | Category level (1=top, 2=second, etc.) |
| `sort_order` | INT | NOT NULL, DEFAULT 0 | Display order |
| `icon` | VARCHAR(255) | | Category icon URL |
| `status` | TINYINT | NOT NULL, DEFAULT 1 | Status: 0=disabled, 1=enabled |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update timestamp |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag |

**Indexes**:
- `idx_parent_id`: Fast retrieval of sub-categories

**Tree Structure Example**:
```
Electronics (id=1, parent_id=0, level=1)
  ├─ Phones (id=6, parent_id=1, level=2)
  ├─ Laptops (id=7, parent_id=1, level=2)
  └─ Accessories (id=8, parent_id=1, level=2)
```

---

### 3.2 `product_spu` - Product SPU Table

SPU (Standard Product Unit) represents product abstract information.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | SPU unique identifier |
| `shop_id` | BIGINT | NOT NULL | Merchant shop ID |
| `category_id` | BIGINT | | Product category ID |
| `brand_name` | VARCHAR(64) | | Brand name |
| `title` | VARCHAR(255) | NOT NULL | Product title |
| `sub_title` | VARCHAR(255) | | Product subtitle |
| `main_image` | VARCHAR(255) | | Main product image URL |
| `detail_text` | TEXT | | Product detail description (HTML) |
| `status` | TINYINT | NOT NULL, DEFAULT 0 | Status: 0=draft, 1=on_shelf, 2=off_shelf |
| `audit_status` | TINYINT | NOT NULL, DEFAULT 0 | Audit: 0=pending, 1=approved, 2=rejected |
| `min_price` | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | Minimum SKU price |
| `max_price` | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | Maximum SKU price |
| `sales_count` | INT | NOT NULL, DEFAULT 0 | Total sales count |
| `like_count` | INT | NOT NULL, DEFAULT 0 | Number of likes |
| `favorite_count` | INT | NOT NULL, DEFAULT 0 | Number of favorites |
| `browse_count` | INT | NOT NULL, DEFAULT 0 | Browse count |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update timestamp |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag |

**Indexes**:
- `idx_shop_id`: Query products by shop
- `idx_category_id`: Query products by category
- `idx_status`: Filter by product status
- `idx_audit_status`: Admin audit queries
- `idx_sales_count`: Sort by popularity
- `idx_create_time`: Sort by newest

**Business Rules**:
- Products require admin approval before listing
- min_price and max_price are automatically computed from SKUs

---

### 3.3 `product_sku` - Product SKU Table

SKU (Stock Keeping Unit) represents specific product variants with inventory.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | SKU unique identifier |
| `spu_id` | BIGINT | NOT NULL | Parent SPU ID |
| `sku_code` | VARCHAR(64) | NOT NULL, UNIQUE | SKU code (globally unique) |
| `sku_name` | VARCHAR(255) | | SKU name (e.g., "Red / XL") |
| `sale_price` | DECIMAL(10,2) | NOT NULL | Current selling price |
| `origin_price` | DECIMAL(10,2) | DEFAULT 0.00 | Original price (for discount display) |
| `stock` | INT | NOT NULL, DEFAULT 0 | Available inventory |
| `lock_stock` | INT | NOT NULL, DEFAULT 0 | Locked inventory (pending orders) |
| `warning_stock` | INT | NOT NULL, DEFAULT 10 | Low stock warning threshold |
| `image_url` | VARCHAR(255) | | SKU-specific image |
| `spec_json` | VARCHAR(500) | | Specification JSON: {"color":"red","size":"XL"} |
| `status` | TINYINT | NOT NULL, DEFAULT 1 | Status: 0=disabled, 1=enabled |
| `version` | INT | NOT NULL, DEFAULT 0 | Optimistic lock version for stock updates |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update timestamp |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag |

**Indexes**:
- `idx_spu_id`: Query SKUs by SPU
- `idx_sku_code`: Fast SKU lookup
- `idx_sale_price`: Price range queries

**Optimistic Locking**:
- `version` field prevents overselling through concurrent stock updates
- Update query: `UPDATE product_sku SET stock = stock - ?, version = version + 1 WHERE id = ? AND version = ?`

---

### 3.4 `product_image` - Product Image Table

Stores multiple images for SPU and SKU.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Image unique identifier |
| `spu_id` | BIGINT | NOT NULL | SPU ID |
| `sku_id` | BIGINT | DEFAULT NULL | SKU ID (NULL for SPU images) |
| `image_url` | VARCHAR(255) | NOT NULL | Image URL |
| `image_type` | TINYINT | NOT NULL, DEFAULT 1 | Type: 1=spu_main, 2=spu_detail, 3=sku |
| `sort_order` | INT | NOT NULL, DEFAULT 0 | Display order |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Upload timestamp |

**Indexes**:
- `idx_spu_id`: Retrieve all images for a product

---

## 4. Cart & Address Module

### 4.1 `cart_item` - Shopping Cart Table

Stores user shopping cart items.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Cart item unique identifier |
| `user_id` | BIGINT | NOT NULL | User ID |
| `sku_id` | BIGINT | NOT NULL | Product SKU ID |
| `quantity` | INT | NOT NULL, DEFAULT 1 | Quantity |
| `checked` | TINYINT | NOT NULL, DEFAULT 1 | Selected for checkout: 0=no, 1=yes |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Added to cart time |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last modification time |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag |

**Indexes**:
- `uk_user_sku`: Unique constraint on (user_id, sku_id)
- `idx_user_id`: Query cart by user

**Business Rules**:
- Each user can have only one cart item per SKU
- Adding existing SKU increments quantity instead of creating duplicate

---

### 4.2 `user_address` - User Delivery Address Table

Stores user shipping addresses.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Address unique identifier |
| `user_id` | BIGINT | NOT NULL | User ID |
| `receiver_name` | VARCHAR(64) | NOT NULL | Recipient name |
| `receiver_phone` | VARCHAR(20) | NOT NULL | Recipient phone |
| `province` | VARCHAR(64) | NOT NULL | Province/state |
| `city` | VARCHAR(64) | NOT NULL | City |
| `district` | VARCHAR(64) | NOT NULL | District/county |
| `detail_address` | VARCHAR(255) | NOT NULL | Detailed street address |
| `postal_code` | VARCHAR(20) | | Postal/ZIP code |
| `is_default` | TINYINT | NOT NULL, DEFAULT 0 | Default address: 0=no, 1=yes |
| `tag_name` | VARCHAR(20) | | Address tag (e.g., "Home", "Office") |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update timestamp |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag |

**Indexes**:
- `idx_user_id`: Query addresses by user

**Business Rules**:
- Users can have multiple addresses
- Only one address can be set as default per user

---

## 5. Order & Payment Module

### 5.1 `order_info` - Order Master Table

Main order information table.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Order unique identifier |
| `order_no` | VARCHAR(64) | NOT NULL, UNIQUE | Order number (human-readable) |
| `user_id` | BIGINT | NOT NULL | Buyer user ID |
| `shop_id` | BIGINT | NOT NULL | Merchant shop ID |
| `total_amount` | DECIMAL(10,2) | NOT NULL | Original total amount |
| `discount_amount` | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | Discount amount |
| `pay_amount` | DECIMAL(10,2) | NOT NULL | Final payment amount |
| `freight_amount` | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | Shipping fee |
| `order_status` | TINYINT | NOT NULL, DEFAULT 0 | Order status (see below) |
| `pay_status` | TINYINT | NOT NULL, DEFAULT 0 | Payment status: 0=unpaid, 1=paid, 2=refunded |
| `source_type` | TINYINT | NOT NULL, DEFAULT 1 | Order source: 1=normal, 2=agent, 3=plan |
| `receiver_name` | VARCHAR(64) | NOT NULL | Recipient name (snapshot) |
| `receiver_phone` | VARCHAR(20) | NOT NULL | Recipient phone (snapshot) |
| `receiver_address` | VARCHAR(255) | NOT NULL | Delivery address (snapshot) |
| `remark` | VARCHAR(255) | | Buyer notes |
| `pay_time` | DATETIME | | Payment timestamp |
| `delivery_time` | DATETIME | | Shipment timestamp |
| `finish_time` | DATETIME | | Completion timestamp |
| `cancel_time` | DATETIME | | Cancellation timestamp |
| `cancel_reason` | VARCHAR(255) | | Cancellation reason |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Order creation time |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag |

**Order Status Values**:
- `0`: Unpaid - waiting for payment
- `1`: To Ship - paid, waiting for merchant to ship
- `2`: To Receive - shipped, in transit
- `3`: Completed - received and confirmed by buyer
- `4`: Cancelled - cancelled before payment or by timeout
- `5`: Refunding - refund requested, pending approval
- `6`: Refunded - refund completed

**Indexes**:
- `idx_user_id`: Query orders by buyer
- `idx_shop_id`: Query orders by merchant
- `idx_order_status`: Filter by order status
- `idx_create_time`: Sort by order date

**Business Rules**:
- Orders unpaid for 30 minutes are auto-cancelled
- Address information is snapshotted to prevent changes after order placement

---

### 5.2 `order_item` - Order Item Table

Order detail items (one row per SKU in order).

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Order item unique identifier |
| `order_id` | BIGINT | NOT NULL | Order master ID |
| `order_no` | VARCHAR(64) | NOT NULL | Order number (redundant for quick lookup) |
| `spu_id` | BIGINT | NOT NULL | Product SPU ID |
| `sku_id` | BIGINT | NOT NULL | Product SKU ID |
| `product_title` | VARCHAR(255) | NOT NULL | Product title (snapshot) |
| `sku_name` | VARCHAR(255) | | SKU name (snapshot) |
| `sku_spec_json` | VARCHAR(500) | | SKU specifications (snapshot) |
| `product_image` | VARCHAR(255) | | Product image URL (snapshot) |
| `sale_price` | DECIMAL(10,2) | NOT NULL | Unit price at purchase time |
| `quantity` | INT | NOT NULL | Purchase quantity |
| `total_amount` | DECIMAL(10,2) | NOT NULL | Item subtotal (price × quantity) |
| `review_status` | TINYINT | NOT NULL, DEFAULT 0 | Review status: 0=not_reviewed, 1=reviewed |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update timestamp |

**Indexes**:
- `idx_order_id`: Query items by order
- `idx_order_no`: Query items by order number

**Business Rules**:
- Product information is snapshotted to preserve historical data
- Each order can contain multiple items from the same shop

---

### 5.3 `order_operate_log` - Order Operation Log Table

Audit trail for order status changes and operations.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Log unique identifier |
| `order_id` | BIGINT | NOT NULL | Order ID |
| `order_no` | VARCHAR(64) | NOT NULL | Order number |
| `before_status` | TINYINT | | Status before change |
| `after_status` | TINYINT | | Status after change |
| `operator_id` | BIGINT | | User ID who performed the operation |
| `operator_role` | VARCHAR(32) | | Operator role (BUYER/MERCHANT/ADMIN/SYSTEM) |
| `operate_type` | VARCHAR(64) | NOT NULL | Operation type (CREATE/PAY/SHIP/CONFIRM/CANCEL) |
| `remark` | VARCHAR(255) | | Operation notes |
| `operate_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Operation timestamp |

**Indexes**:
- `idx_order_no`: Query operation history by order

**Use Cases**:
- Track who and when order status was changed
- Audit trail for disputes
- System operations (e.g., auto-cancel) have operator_id = NULL

---

### 5.4 `payment_record` - Payment Record Table

Payment transaction records.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Payment unique identifier |
| `order_no` | VARCHAR(64) | NOT NULL | Order number |
| `pay_no` | VARCHAR(64) | NOT NULL, UNIQUE | Payment number (unique transaction ID) |
| `user_id` | BIGINT | NOT NULL | Payer user ID |
| `pay_amount` | DECIMAL(10,2) | NOT NULL | Payment amount |
| `pay_method` | TINYINT | NOT NULL, DEFAULT 1 | Payment method: 1=simulated_pay |
| `pay_status` | TINYINT | NOT NULL | Status: 0=pending, 1=success, 2=failed |
| `third_trade_no` | VARCHAR(128) | | Third-party payment platform transaction ID |
| `pay_time` | DATETIME | | Successful payment timestamp |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Record creation time |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Indexes**:
- `idx_order_no`: Query payments by order
- `idx_user_id`: Query user payment history

**Business Rules**:
- Currently supports simulated payment for demo purposes
- Can be extended for real payment gateways (Alipay, WeChat Pay, etc.)

---

### 5.5 `inventory_log` - Inventory Change Log Table

Tracks all stock changes for auditing and debugging.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Log unique identifier |
| `sku_id` | BIGINT | NOT NULL | SKU ID |
| `order_no` | VARCHAR(64) | | Related order number (if applicable) |
| `change_count` | INT | NOT NULL | Change amount (positive or negative) |
| `before_stock` | INT | NOT NULL | Stock before change |
| `after_stock` | INT | NOT NULL | Stock after change |
| `operate_type` | VARCHAR(32) | NOT NULL | Operation: LOCK/UNLOCK/DEDUCT/RETURN |
| `remark` | VARCHAR(255) | | Operation notes |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Operation timestamp |

**Indexes**:
- `idx_sku_id`: Query stock history by SKU
- `idx_order_no`: Query stock changes by order

**Operation Types**:
- `LOCK`: Reserve stock when order is created
- `UNLOCK`: Release locked stock when order is cancelled
- `DEDUCT`: Reduce actual stock when order is paid
- `RETURN`: Return stock when refund is processed

---

## 6. Review Module

### 6.1 `product_review` - Product Review Table

Customer reviews and ratings for purchased products.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Review unique identifier |
| `order_item_id` | BIGINT | NOT NULL | Order item ID (ensures verified purchase) |
| `order_no` | VARCHAR(64) | NOT NULL | Order number |
| `user_id` | BIGINT | NOT NULL | Reviewer user ID |
| `spu_id` | BIGINT | NOT NULL | Product SPU ID |
| `sku_id` | BIGINT | NOT NULL | Product SKU ID |
| `score` | TINYINT | NOT NULL | Rating score (1-5 stars) |
| `content` | VARCHAR(1000) | | Review content text |
| `image_urls` | VARCHAR(2000) | | Review images (JSON array or comma-separated) |
| `anonymous_flag` | TINYINT | NOT NULL, DEFAULT 0 | Anonymous review: 0=no, 1=yes |
| `reply_content` | VARCHAR(1000) | | Merchant reply content |
| `reply_time` | DATETIME | | Merchant reply timestamp |
| `review_status` | TINYINT | NOT NULL, DEFAULT 1 | Status: 0=hidden, 1=visible |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Review submission time |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag |

**Indexes**:
- `idx_spu_id`: Query reviews by product
- `idx_user_id`: Query reviews by user
- `idx_order_no`: Verify review eligibility

**Business Rules**:
- Only buyers who have purchased and received the product can review
- One review per order item
- Reviews can be hidden by admin if they violate policies

---

## 7. User Behavior Module

### 7.1 `user_favorite` - User Favorites Table

User's favorite products list (wishlist).

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Favorite unique identifier |
| `user_id` | BIGINT | NOT NULL | User ID |
| `spu_id` | BIGINT | NOT NULL | Product SPU ID |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Favorite timestamp |

**Indexes**:
- `uk_user_spu`: Unique constraint on (user_id, spu_id)
- `idx_user_id`: Query favorites by user

---

### 7.2 `user_browse_history` - User Browse History Table

Tracks user product browsing behavior for personalization.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | History unique identifier |
| `user_id` | BIGINT | NOT NULL | User ID |
| `spu_id` | BIGINT | NOT NULL | Product SPU ID |
| `browse_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Browse timestamp |

**Indexes**:
- `idx_user_id`: Query history by user
- `idx_browse_time`: Sort by recency

**Use Cases**:
- Personalized recommendations
- "Recently viewed" product list
- User behavior analysis

---

## 8. AI / Intelligence Module

### 8.1 `product_knowledge_doc` - Product Knowledge Document Table

RAG (Retrieval-Augmented Generation) knowledge base for AI Q&A.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Document unique identifier |
| `spu_id` | BIGINT | NOT NULL | Related product SPU ID |
| `title` | VARCHAR(255) | | Document title |
| `content` | TEXT | NOT NULL | Document content (product info, FAQ, etc.) |
| `source_type` | VARCHAR(32) | | Source: PRODUCT_DESC/FAQ/AFTER_SALE |
| `status` | TINYINT | NOT NULL, DEFAULT 1 | Status: 0=disabled, 1=enabled |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update timestamp |

**Indexes**:
- `idx_spu_id`: Query knowledge documents by product

**Use Cases**:
- RAG-based product Q&A chatbot
- Vector embedding storage (requires external vector database)
- Product information retrieval

---

### 8.2 `ai_chat_session` - AI Chat Session Table

AI conversation session management.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Session unique identifier |
| `user_id` | BIGINT | NOT NULL | User ID |
| `session_type` | VARCHAR(32) | NOT NULL | Type: RAG/AGENT/PLAN |
| `spu_id` | BIGINT | | Related product ID (for RAG sessions) |
| `title` | VARCHAR(128) | | Session title |
| `status` | TINYINT | NOT NULL, DEFAULT 1 | Status: 0=ended, 1=active |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Session start time |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last activity time |

**Indexes**:
- `idx_user_id`: Query sessions by user

**Session Types**:
- `RAG`: Product Q&A chatbot
- `AGENT`: AI shopping assistant
- `PLAN`: Scheduled shopping planner

---

### 8.3 `ai_chat_message` - AI Chat Message Table

Individual messages within AI chat sessions.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Message unique identifier |
| `session_id` | BIGINT | NOT NULL | Chat session ID |
| `role` | VARCHAR(16) | NOT NULL | Message role: user/assistant/system |
| `content` | TEXT | NOT NULL | Message content |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Message timestamp |

**Indexes**:
- `idx_session_id`: Query messages by session

---

### 8.4 `shopping_plan` - Shopping Plan Table

User's scheduled shopping plans (buy later functionality).

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Plan unique identifier |
| `user_id` | BIGINT | NOT NULL | User ID |
| `plan_name` | VARCHAR(128) | NOT NULL | Plan name (e.g., "Birthday gifts") |
| `trigger_time` | DATETIME | | Scheduled reminder time |
| `plan_status` | TINYINT | NOT NULL, DEFAULT 0 | Status: 0=created, 1=reminded, 2=executed, 3=cancelled |
| `budget_amount` | DECIMAL(10,2) | | Budget limit |
| `remark` | VARCHAR(500) | | Plan notes |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Plan creation time |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |
| `deleted` | TINYINT | NOT NULL, DEFAULT 0 | Soft delete flag |

**Indexes**:
- `idx_user_id`: Query plans by user
- `idx_trigger_time`: Schedule-based queries for reminder system

---

### 8.5 `shopping_plan_item` - Shopping Plan Item Table

Individual items within a shopping plan.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Plan item unique identifier |
| `plan_id` | BIGINT | NOT NULL | Shopping plan ID |
| `keyword` | VARCHAR(128) | | Search keyword for the item |
| `category_id` | BIGINT | | Desired category |
| `expected_price_min` | DECIMAL(10,2) | | Minimum expected price |
| `expected_price_max` | DECIMAL(10,2) | | Maximum expected price |
| `quantity` | INT | NOT NULL, DEFAULT 1 | Desired quantity |
| `matched_spu_id` | BIGINT | | AI-matched product SPU ID |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Item creation time |

**Indexes**:
- `idx_plan_id`: Query items by plan

**Use Cases**:
- AI agent matches keywords to products
- Price monitoring and alerts
- Automated purchasing when conditions are met

---

### 8.6 `agent_task` - Agent Task Table

AI agent task execution records.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Task unique identifier |
| `user_id` | BIGINT | NOT NULL | User ID |
| `task_type` | VARCHAR(32) | NOT NULL | Type: SHOPPING/RECOMMEND/COPYWRITING |
| `user_prompt` | TEXT | NOT NULL | User's natural language request |
| `task_status` | TINYINT | NOT NULL, DEFAULT 0 | Status: 0=created, 1=running, 2=completed, 3=failed |
| `result_json` | TEXT | | Task result in JSON format |
| `create_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Task creation time |
| `update_time` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Indexes**:
- `idx_user_id`: Query tasks by user

**Task Types**:
- `SHOPPING`: AI-powered product search and comparison
- `RECOMMEND`: Personalized product recommendations
- `COPYWRITING`: AI-generated product descriptions

---

## 9. Indexing Strategy

### Primary Indexes
All tables use `BIGINT AUTO_INCREMENT PRIMARY KEY` for efficient row lookup and join operations.

### Foreign Key Indexes
- All foreign key columns have individual indexes for join performance
- Example: `idx_user_id`, `idx_shop_id`, `idx_spu_id`

### Business Query Indexes
- **Status columns**: Frequently filtered fields (order_status, apply_status)
- **Time-based**: Support sorting by creation/update time
- **Enumeration**: Category, user type for filtering

### Unique Constraints
- Natural unique keys: username, phone, sku_code, order_no
- Composite unique keys: (user_id, sku_id) in cart, (user_id, spu_id) in favorites

---

## 10. Data Consistency Rules

### Soft Delete
All business tables use `deleted` flag instead of physical deletion to:
- Preserve historical data
- Support data recovery
- Enable audit trails

### Optimistic Locking
`product_sku.version` field prevents overselling through:
```sql
UPDATE product_sku
SET stock = stock - ?, lock_stock = lock_stock + ?, version = version + 1
WHERE id = ? AND version = ? AND stock >= ?
```

### Timestamp Tracking
- `create_time`: Record creation timestamp
- `update_time`: Automatically updated on modification
- Business timestamps: pay_time, delivery_time, finish_time

### Data Snapshot
Critical data is copied at transaction time to prevent inconsistencies:
- Order address information
- Order item product details (title, price, image)
- Preserves historical accuracy even if source data changes

---

## 11. Relationships Summary

```
sys_user (1) ----< (N) sys_user_role (N) >---- (1) sys_role
sys_role (1) ----< (N) sys_role_permission (N) >---- (1) sys_permission

sys_user (1) ----< (N) merchant_apply
sys_user (1) ---- (1) merchant_shop
merchant_shop (1) ----< (N) product_spu
product_spu (1) ----< (N) product_sku
product_spu (1) ----< (N) product_image

sys_user (1) ----< (N) cart_item (N) >---- (1) product_sku
sys_user (1) ----< (N) user_address

sys_user (1) ----< (N) order_info (N) >---- (1) merchant_shop
order_info (1) ----< (N) order_item
order_info (1) ----< (N) order_operate_log
order_info (1) ----< (N) payment_record
order_item (1) ---- (0..1) product_review

sys_user (1) ----< (N) user_favorite (N) >---- (1) product_spu
sys_user (1) ----< (N) user_browse_history (N) >---- (1) product_spu

product_spu (1) ----< (N) product_knowledge_doc
sys_user (1) ----< (N) ai_chat_session
ai_chat_session (1) ----< (N) ai_chat_message

sys_user (1) ----< (N) shopping_plan
shopping_plan (1) ----< (N) shopping_plan_item

sys_user (1) ----< (N) agent_task
```

---

## 12. Security Considerations

### Password Storage
- BCrypt hashing with cost factor 10
- Never store plain text passwords
- Password reset requires email/SMS verification

### SQL Injection Prevention
- MyBatis-Plus parameter binding
- No dynamic SQL concatenation in application code

### Data Privacy
- Sensitive data: phone, email have unique indexes for quick lookup
- Personal data subject to GDPR/local privacy regulations
- Anonymous review option for user privacy

### Audit Trails
- Operation logs for orders
- Inventory change logs
- All user actions timestamped with operator information

---

## 13. Performance Optimization

### Database-Level
- InnoDB engine for ACID transactions
- utf8mb4 for international character support
- Connection pooling (HikariCP with 20 max connections)

### Application-Level
- Redis caching for hot products, cart data
- Read-write separation potential (master-slave replication)
- Pagination for large result sets

### Query Optimization
- Covering indexes for frequent queries
- Avoid SELECT * in production code
- Use EXPLAIN to analyze query plans

---

## Appendix: Initial Data

### Default Admin Account
```sql
INSERT IGNORE INTO sys_user (id, username, password, nickname, user_type, status) VALUES
(1, 'admin', '$2a$10$77fqFla9dMpEi4DR1J8WBO6qaJKrR8WChf8I5jwugvpkVseMI1yBe', 'System Admin', 3, 1);
```
**Credentials**: username=`admin`, password=`admin123`

### Default Roles
```sql
INSERT IGNORE INTO sys_role (id, role_code, role_name) VALUES
(1, 'ROLE_BUYER', 'Buyer'),
(2, 'ROLE_MERCHANT', 'Merchant'),
(3, 'ROLE_ADMIN', 'Administrator');
```

### Default Categories
Top-level: Electronics, Clothing, Home & Living, Books, Food & Beverage
Second-level: Phones, Laptops, Accessories (under Electronics), Men, Women (under Clothing)

---

**Document Version**: 1.0
**Last Updated**: 2025-03-15
**Schema File**: `src/main/resources/schema.sql`
