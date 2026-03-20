# One-Stop Intelligent E-Commerce Platform - Development Guide

> Version: 1.0.0 | Last Updated: 2026-03-14 | Status: P0 Core Complete

---

## 1. Project Overview

### 1.1 Background

This is a course-design-level full-stack e-commerce platform targeting three user roles: **Buyer**, **Merchant**, and **Administrator**. The system implements a complete business loop from product listing to order fulfillment, with planned AI-powered intelligent features for Phase 2.

### 1.2 Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Framework | Spring Boot | 3.3.4 |
| ORM | MyBatis-Plus | 3.5.7 |
| Security | Spring Security + JWT (jjwt) | 6.x / 0.12.5 |
| Cache | Spring Data Redis | 3.3.x |
| Database | MySQL | 8.x |
| API Doc | Knife4j (OpenAPI 3) | 4.4.0 |
| Build | Maven Wrapper | 3.9.x |
| Language | Java | 17 |

### 1.3 Architecture Style

- **Monolithic** Spring Boot application (appropriate for course design scope)
- **RESTful** API design, stateless JWT authentication
- **Layered**: Controller → Service → Mapper → Entity
- **Modular**: Each business domain isolated under `modules/`

---

## 2. Project Structure

```
com.helloworld.onlineshopping
├── OnlineShoppingApplication.java          # Entry point, @EnableScheduling
├── common/
│   ├── api/            # Result<T>, PageQuery, PageResult<T>
│   ├── config/         # MyBatisPlusConfig, RedisConfig
│   ├── entity/         # BaseEntity (id, createTime, updateTime, deleted)
│   ├── enums/          # OrderStatusEnum, PayStatusEnum, UserTypeEnum, ProductStatusEnum
│   ├── exception/      # BusinessException, GlobalExceptionHandler
│   ├── security/       # SecurityConfig, JwtAuthenticationFilter, LoginUser, SecurityUtil
│   └── utils/          # JwtUtil, OrderNoGenerator
├── modules/
│   ├── auth/           # Registration, Login, JWT token issuance
│   ├── user/           # User profile, password management
│   ├── merchant/       # Shop application, admin audit, shop info
│   ├── product/        # SPU/SKU CRUD, category tree, product search
│   ├── cart/           # Shopping cart (Redis + MySQL hybrid)
│   ├── address/        # Delivery address CRUD
│   ├── order/          # Order lifecycle, payment simulation, timeout scheduler
│   ├── review/         # Product reviews and statistics
│   └── admin/          # Dashboard analytics, product audit
└── resources/
    ├── application.yml # All configuration
    └── schema.sql      # 27 tables + seed data
```

---

## 3. Current Progress (P0 - Complete)

### 3.1 Module Completion Matrix

| Module | Entity | Mapper | Service | Controller | DTO/VO | Status |
|--------|--------|--------|---------|------------|--------|--------|
| Auth | - | - | AuthService | AuthController | LoginDTO, RegisterDTO, LoginVO | DONE |
| User | UserEntity, RoleEntity, UserRoleEntity | 3 Mappers | UserService | UserController | UpdateProfileDTO, ChangePasswordDTO, UserInfoVO | DONE |
| Merchant | MerchantApplyEntity, MerchantShopEntity | 2 Mappers | MerchantService | MerchantController | MerchantApplyDTO, MerchantAuditDTO, MerchantApplyVO, MerchantShopVO | DONE |
| Product | ProductSpuEntity, ProductSkuEntity, CategoryEntity, ProductImageEntity | 4 Mappers | ProductService, CategoryService | ProductController, CategoryController | ProductSpuCreateDTO, ProductSkuDTO, ProductSearchDTO, CategoryCreateDTO, ProductSimpleVO, ProductDetailVO, ProductSkuVO, CategoryVO | DONE |
| Cart | CartItemEntity | CartItemMapper | CartService | CartController | CartAddDTO, CartUpdateDTO, CartItemVO, CartVO | DONE |
| Address | UserAddressEntity | UserAddressMapper | AddressService | AddressController | AddressCreateDTO, AddressUpdateDTO, AddressVO | DONE |
| Order | OrderEntity, OrderItemEntity, OrderOperateLogEntity, PaymentRecordEntity, InventoryLogEntity | 5 Mappers | OrderService | OrderController | OrderSubmitDTO, OrderQueryDTO, OrderSubmitVO, OrderDetailVO, OrderListVO, OrderItemVO, PaymentVO | DONE |
| Review | ProductReviewEntity | ProductReviewMapper | ReviewService | ReviewController | ReviewCreateDTO, ReviewQueryDTO, ReviewVO, ReviewStatisticVO | DONE |
| Admin | - | - | AdminService | AdminController | DashboardVO | DONE |

### 3.2 API Endpoints Summary

#### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | User registration |
| POST | `/api/auth/login` | Login, returns JWT |

#### User (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/user/me` | Get current user info |
| PUT | `/api/user/profile` | Update profile |
| PUT | `/api/user/password` | Change password |

#### Merchant
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/merchant/apply` | BUYER | Apply to become merchant |
| GET | `/api/merchant/shop/current` | MERCHANT | Get own shop info |
| GET | `/api/merchant/apply/list` | ADMIN | List pending applications |
| POST | `/api/merchant/apply/{id}/audit` | ADMIN | Approve/reject application |

#### Product (Search is public)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/products` | Public | Search & paginate products |
| GET | `/api/products/{spuId}` | Public | Product detail |
| POST | `/api/products` | MERCHANT | Create product |
| PUT | `/api/products/{spuId}/on-shelf` | MERCHANT | Publish product |
| PUT | `/api/products/{spuId}/off-shelf` | MERCHANT | Unpublish product |
| GET | `/api/categories` | Public | Category tree |
| POST | `/api/categories` | ADMIN | Create category |

#### Cart (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/cart/add` | Add item to cart |
| POST | `/api/cart/batch-add` | Batch add items to cart |
| GET | `/api/cart/list` | Get cart contents |
| PUT | `/api/cart/update` | Update quantity/checked |
| DELETE | `/api/cart/item/{skuId}` | Remove item |

#### Address (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/address` | Create address |
| GET | `/api/address/list` | List addresses |
| PUT | `/api/address` | Update address |
| DELETE | `/api/address/{id}` | Delete address |

#### Order (Authenticated)
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/order/submit` | BUYER | Submit order from cart |
| GET | `/api/order/{orderNo}` | BUYER | Order detail |
| GET | `/api/order/list` | BUYER | Order list (paginated) |
| POST | `/api/order/{orderNo}/cancel` | BUYER | Cancel unpaid order |
| POST | `/api/order/{orderNo}/pay` | BUYER | Simulate payment |
| POST | `/api/order/{orderNo}/confirm-receive` | BUYER | Confirm delivery |
| POST | `/api/order/{orderNo}/deliver` | MERCHANT | Ship order |

#### Review
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/review` | BUYER | Create review |
| GET | `/api/review/product/{spuId}` | Public | Product reviews |
| GET | `/api/review/product/{spuId}/statistics` | Public | Review statistics |

#### Admin
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/dashboard` | ADMIN | Platform statistics |
| POST | `/api/admin/product/{spuId}/approve` | ADMIN | Approve product |
| POST | `/api/admin/product/{spuId}/reject` | ADMIN | Reject product |

### 3.3 Key Design Decisions

#### Order State Machine
```
 [Create] → UNPAID(0)
                ├── pay ────→ TO_SHIP(1) ── deliver ──→ TO_RECEIVE(2) ── confirm ──→ COMPLETED(3)
                └── cancel ─→ CANCELLED(4)
                                TO_SHIP/TO_RECEIVE ── refund ──→ REFUNDING(5) ──→ REFUNDED(6)
```
All transitions are logged in `order_operate_log` with before/after status, operator, and timestamp.

#### Stock Deduction (Optimistic Locking)
```sql
UPDATE product_sku
SET stock = stock - #{count}, lock_stock = lock_stock + #{count}, version = version + 1
WHERE id = #{skuId} AND stock >= #{count} AND version = #{version};
```
- Returns affected rows = 0 on conflict → throws BusinessException
- No distributed lock required for course-design scale

#### Stock Pre-Lock (Redis Lua, Batch)
- Redis hash key: `stock:sku:{skuId}` with fields `stock` and `lock`
- Submit order flow pre-locks stock in Redis using Lua (`lock-stock.lua`) before DB update
- Pay order deducts Redis lock using Lua (`deduct-lock.lua`)
- Cancel order releases Redis lock using Lua (`unlock-stock.lua`)
- Scripts are located in `src/main/resources/scripts/`

#### Order Timeout Auto-Cancel
- On order creation: `ZADD order:timeout:zset {expireTimestamp} {orderNo}`
- Scheduler runs every 60s: `ZRANGEBYSCORE` for expired entries
- Triggers `doCancelOrder()` which unlocks stock and logs operation

#### Redis Key Design
| Key Pattern | Type | TTL | Usage |
|-------------|------|-----|-------|
| `cart:user:{userId}` | Hash | - | Shopping cart cache |
| `order:timeout:zset` | ZSet | - | Order timeout queue (score = expire timestamp) |
| `stock:sku:{skuId}` | Hash | - | SKU stock pre-lock (fields: stock, lock) |

---

## 4. Database Schema

### 4.1 ER Domain Grouping

| Domain | Tables | Count |
|--------|--------|-------|
| User & Auth | sys_user, sys_role, sys_permission, sys_user_role, sys_role_permission | 5 |
| Merchant | merchant_apply, merchant_shop | 2 |
| Product | product_spu, product_sku, product_category, product_image | 4 |
| Cart & Address | cart_item, user_address | 2 |
| Order & Payment | order_info, order_item, order_operate_log, payment_record, inventory_log | 5 |
| Review | product_review | 1 |
| Behavior | user_favorite, user_browse_history | 2 |
| AI/Intelligence | product_knowledge_doc, ai_chat_session, ai_chat_message, shopping_plan, shopping_plan_item, agent_task | 6 |
| **Total** | | **27** |

### 4.2 Conventions
- All primary keys: `BIGINT AUTO_INCREMENT`
- Timestamps: `create_time`, `update_time` with MySQL auto-fill
- Soft delete: `deleted TINYINT DEFAULT 0`
- Money: `DECIMAL(10,2)` — never FLOAT/DOUBLE
- Optimistic lock: `version INT` on `product_sku`
- High-frequency columns indexed

### 4.3 Seed Data
- 3 default roles: `ROLE_BUYER`, `ROLE_MERCHANT`, `ROLE_ADMIN`
- 1 admin user: `admin` / `admin123`
- 10 product categories (2-level hierarchy)

---

## 5. Development Environment Setup

### 5.1 Prerequisites
- JDK 17+
- MySQL 8.x (create database `online_shopping`)
- Redis 6.x+
- Maven 3.9+ (or use included `mvnw`)

### 5.2 Quick Start
```bash
# 1. Initialize database
mysql -u root -p < src/main/resources/schema.sql

# 2. Edit connection in src/main/resources/application.yml
#    - spring.datasource.url / username / password
#    - spring.data.redis.host / port

# 3. Build & Run
./mvnw spring-boot:run

# 4. Access API docs
open http://localhost:8080/doc.html
```

### 5.3 Default Accounts
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |

---

## 6. Incremental Expansion Plan (P1 & P2)

See Section 7 "Parallel Worktree Plan" for detailed multi-branch development guide.

### P1 Enhancements
- Favorites & Browse History
- Hot Product Recommendation
- Merchant Data Statistics
- Coupon System

### P2 AI Features
- RAG Product Q&A
- AI Copywriting
- AI Review Summary
- Agent Shopping
- Planned Shopping

---

## 7. Parallel Worktree Development Plan

### Branch Strategy
```
master (stable, P0 complete)
  ├── feat/favorite-behavior     ← Worktree A
  ├── feat/recommendation        ← Worktree B
  ├── feat/rag-qa                ← Worktree C
  ├── feat/ai-copywriting        ← Worktree D
  └── feat/agent-shopping        ← Worktree E
```

Each worktree works independently on a feature branch. Merge back to master when complete.

### Worktree Commands
```bash
# Create worktrees
git worktree add ../online-shopping-wt-favorite   feat/favorite-behavior
git worktree add ../online-shopping-wt-recommend   feat/recommendation
git worktree add ../online-shopping-wt-rag         feat/rag-qa
git worktree add ../online-shopping-wt-ai-copy     feat/ai-copywriting
git worktree add ../online-shopping-wt-agent       feat/agent-shopping

# After feature complete, merge back
git checkout master
git merge feat/favorite-behavior
git merge feat/recommendation
# ...
```

### Detailed Task Breakdown Per Worktree

See next section for copy-paste-ready prompts.

---

## 8. Appendix: Commit Convention

Format: `<type>(<scope>): <description>`

| Type | Usage |
|------|-------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code refactoring (no behavior change) |
| `docs` | Documentation |
| `style` | Formatting (no logic change) |
| `test` | Tests |
| `chore` | Build, CI, tooling |
| `perf` | Performance improvement |

Examples:
```
feat(order): add refund workflow with merchant approval
fix(cart): prevent adding out-of-stock SKU to cart
refactor(product): extract price calculation to utility method
docs: add API endpoint table to development guide
```
