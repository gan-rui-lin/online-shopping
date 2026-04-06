# Online Shopping Platform

中文 | [English](README.md)

一个基于 Spring Boot 3 + Vue 3 的一体化智能电商平台，覆盖多端角色（游客、买家、商家、管理员）与智能能力（推荐、购物计划、AI/RAG、Agent）。

## 1. 多端角色与核心功能

| 角色 | 入口端 | 核心能力 |
| --- | --- | --- |
| 游客（Public） | 前台公开页 | 商品检索、分类筛选、商品详情浏览、注册/登录入口 |
| 买家（Buyer） | 买家端 | 购物车、地址管理、下单支付、订单跟踪、评价、收藏、浏览历史 |
| 商家（Merchant） | 商家端 | 商品创建/编辑（SPU/SKU）、上下架、图片绑定、订单处理、店铺经营数据 |
| 管理员（Admin） | 管理后台 | 仪表盘、商家审核、商品审核、类目管理、平台治理 |
| 智能模块（AI/Agent/Plan） | 智能接口与页面 | 智能推荐、购物计划生成、商品知识库与问答、Agent 任务编排 |

## 2. 审核与上架机制（关键业务规则）

- 商家新建商品默认是待审核（auditStatus = 0）。
- 管理员在商品审核页进行通过/拒绝。
- 商品只有审核通过后才允许上架。

## 3. 技术栈

- 后端：Spring Boot 3.3.4, MyBatis-Plus 3.5.7, Spring Security, JWT
- 数据层：MySQL 8, Redis
- 搜索：Elasticsearch（可开关）
- 文档：Knife4j / Swagger
- 前端：Vue 3 + TypeScript + Element Plus（位于 frontend 目录）

## 4. 快速启动

### 4.1 前置依赖

- JDK 17+
- MySQL 8.x
- Redis 6.x+

### 4.2 初始化数据库

```bash
mysql -u root -p < src/main/resources/schema.sql
```

如需清空并重建（Windows）：

```powershell
./scripts/reset-and-init-db.ps1 -DbPassword "your_mysql_password"
```

### 4.3 配置环境变量

```bash
cp .env.example .env
```

常用变量包括：DB_URL、DB_USERNAME、DB_PASSWORD、REDIS_HOST、REDIS_PORT、APP_SEARCH_ES_ENABLED、ES_URIS、FILE_UPLOAD_BASE_DIR、FILE_UPLOAD_PUBLIC_URL、JWT_SECRET。

### 4.4 启动后端

```bash
./mvnw spring-boot:run
```

Windows 一键启动（含 ES 检查）：

```powershell
$env:DB_PASSWORD="your_mysql_password"
./scripts/start-es-and-backend.ps1
```

### 4.5 API 文档

启动后访问：[http://localhost:8080/doc.html](http://localhost:8080/doc.html)

## 5. 默认测试账号

| 类型 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | admin | admin123 |
| 其他测试账号（如 alice_chen、tech_store） | 见 init-data.sql | test123 |

## 6. 项目结构

```text
src/main/java/com/helloworld/onlineshopping/
├── common/             # 通用能力：返回体、安全、配置、枚举、异常
└── modules/
    ├── auth/           # 登录注册、JWT
    ├── user/           # 用户资料、密码
    ├── merchant/       # 商家申请与审核
    ├── product/        # 商品 SPU/SKU、类目、检索、图片
    ├── cart/           # 购物车
    ├── address/        # 收货地址
    ├── order/          # 订单、支付、超时处理
    ├── review/         # 商品评价
    ├── admin/          # 管理后台功能
    ├── recommendation/ # 推荐系统
    ├── plan/           # 购物计划
    ├── ai/             # AI 能力
    └── agent/          # Agent 任务
```

## 7. TODO（规划中）

- TODO: 审核中心增加按店铺/类目/时间区间筛选与批量审核。
- TODO: 商家中心增加经营报表导出（CSV/Excel）。
- TODO: 买家端增加优惠券与营销活动闭环。
- TODO: 智能模块增加多模型路由与效果评估面板。

## 8. 详细文档

- 开发指南：[docs/DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md)
- 数据库设计：[docs/DATABASE_DESIGN.md](docs/DATABASE_DESIGN.md)
- 图片绑定流程：[docs/FILE_IMAGE_WORKFLOW.md](docs/FILE_IMAGE_WORKFLOW.md)
