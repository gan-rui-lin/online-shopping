# Online Shopping Platform

[中文](README.zh-CN.md) | English

An integrated intelligent e-commerce platform built with Spring Boot 3 + Vue 3, covering multi-role clients (public visitor, buyer, merchant, admin) and AI capabilities (recommendation, shopping plan, RAG, and agent workflows).

## 1. Roles and Capabilities

| Role | Client Side | Core Features |
| --- | --- | --- |
| Public Visitor | Public storefront | Product search, category filtering, product detail browsing, register/login entry |
| Buyer | Buyer side | Cart, addresses, order/payment lifecycle, review, favorites, browse history |
| Merchant | Merchant side | Product create/edit (SPU/SKU), on/off shelf, image binding, order handling, shop operations |
| Admin | Admin console | Dashboard, merchant audit, product audit, category governance |
| AI/Agent Modules | Intelligence APIs/pages | Recommendations, shopping plan generation, product knowledge Q&A, agent task orchestration |

## 2. Key Audit Rule

- Newly created merchant products are pending by default (auditStatus = 0).
- Admin reviews products in the audit page and approves/rejects them.
- Only approved products can be put on shelf.

## 3. Tech Stack

- Backend: Spring Boot 3.3.4, MyBatis-Plus 3.5.7, Spring Security, JWT
- Data: MySQL 8, Redis
- Search: Elasticsearch (toggleable)
- API docs: Knife4j / Swagger
- Frontend: Vue 3 + TypeScript + Element Plus (in frontend directory)

## 4. Quick Start

### 4.1 Prerequisites

- JDK 17+
- MySQL 8.x
- Redis 6.x+

### 4.2 Initialize Database

```bash
mysql -u root -p < src/main/resources/schema.sql
```

Windows full reset + seed:

```powershell
./scripts/reset-and-init-db.ps1 -DbPassword "your_mysql_password"
```

### 4.3 Configure Environment

```bash
cp .env.example .env
```

Common variables include DB_URL, DB_USERNAME, DB_PASSWORD, REDIS_HOST, REDIS_PORT, APP_SEARCH_ES_ENABLED, ES_URIS, FILE_UPLOAD_BASE_DIR, FILE_UPLOAD_PUBLIC_URL, JWT_SECRET.

### 4.4 Run Backend

```bash
./mvnw spring-boot:run
```

Windows startup script (with ES checks):

```powershell
$env:DB_PASSWORD="your_mysql_password"
./scripts/start-es-and-backend.ps1
```

### 4.5 API Docs

After startup: [http://localhost:8080/doc.html](http://localhost:8080/doc.html)

## 5. Default Test Accounts

| Type | Username | Password |
| --- | --- | --- |
| Admin | admin | admin123 |
| Other test users (for example alice_chen, tech_store) | See init-data.sql | test123 |

## 6. Project Structure

```text
src/main/java/com/helloworld/onlineshopping/
├── common/             # Shared capability: response, security, config, enums, exceptions
└── modules/
    ├── auth/           # Authentication and JWT
    ├── user/           # User profile and password
    ├── merchant/       # Merchant apply and audit
    ├── product/        # Product SPU/SKU, category, search, images
    ├── cart/           # Shopping cart
    ├── address/        # Shipping address
    ├── order/          # Order and payment lifecycle
    ├── review/         # Product reviews
    ├── admin/          # Admin dashboard and governance
    ├── recommendation/ # Recommendation engine
    ├── plan/           # Shopping plan
    ├── ai/             # AI capabilities
    └── agent/          # Agent workflows
```

## 7. TODO Roadmap

- TODO: Add filter + batch operations in admin audit center.
- TODO: Add merchant report export (CSV/Excel).
- TODO: Add buyer coupon/marketing workflow.
- TODO: Add model routing + evaluation panel in intelligence center.

## 8. More Docs

- Development Guide: [docs/DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md)
- Database Design: [docs/DATABASE_DESIGN.md](docs/DATABASE_DESIGN.md)
- File/Image Workflow: [docs/FILE_IMAGE_WORKFLOW.md](docs/FILE_IMAGE_WORKFLOW.md)
