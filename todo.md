# 需求对齐优先任务清单（P0/P1/Later）

## P0（必须完成，阻塞验收）

### 1. 商品上架审核规则
- 目标：未审核通过商品不可上架；审核通过后可上架；任意状态可下架。
- 验收：
  - 商家调用 `/api/products/{spuId}/on-shelf` 且 `auditStatus != 1` 返回业务错误。
  - 管理员审核通过后，上架成功。
  - 下架接口不受审核状态阻塞。
- 涉及文件：
  - `src/main/java/com/helloworld/onlineshopping/modules/product/service/ProductService.java`
  - `src/test/java/com/helloworld/onlineshopping/modules/flow/BackendSpecFlowIntegrationTest.java`

### 2. 商家商品列表字段对齐（status/auditStatus）
- 目标：`/api/products/my` 返回字段与前端使用一致，包含 `status`、`auditStatus`。
- 验收：
  - 后端 VO 含 `status`、`auditStatus`。
  - 商家商品页可展示上架状态与审核状态。
- 涉及文件：
  - `src/main/java/com/helloworld/onlineshopping/modules/product/vo/ProductSimpleVO.java`
  - `src/main/java/com/helloworld/onlineshopping/modules/product/service/ProductService.java`
  - `src/main/java/com/helloworld/onlineshopping/modules/product/search/EsProductService.java`
  - `frontend/src/types/product.ts`
  - `frontend/src/views/merchant/ProductListView.vue`

### 3. 卖家端智能助手页（/merchant/intelligence）
- 目标：补齐卖家入口，支持商品选择、AI文案、评论摘要、知识库导入。
- 验收：
  - 新增路由 `/merchant/intelligence`。
  - 页面可调用：
    - `/api/products/my`
    - `/api/ai/copywriting/title/{spuId}`
    - `/api/ai/copywriting/selling-points/{spuId}`
    - `/api/ai/review-summary/{spuId}`
    - `/api/ai/copywriting/evaluate/{spuId}`
    - `/api/rag/knowledge/import/{spuId}`
- 涉及文件：
  - `frontend/src/views/merchant/IntelligenceView.vue`
  - `frontend/src/router/index.ts`
  - `frontend/src/layout/MerchantLayout.vue`
  - `frontend/src/i18n/locales/zh-CN.ts`
  - `frontend/src/i18n/locales/en-US.ts`

### 4. 卖家端评价管理页（/merchant/reviews）
- 目标：卖家可按本店商品查看评价并回复。
- 验收：
  - 新增路由 `/merchant/reviews`。
  - 页面支持选择本店SPU，调用 `/api/review/product/{spuId}` 展示评价。
  - 未回复评价可调用 `/api/review/{reviewId}/reply`。
- 涉及文件：
  - `frontend/src/views/merchant/ReviewManageView.vue`
  - `frontend/src/router/index.ts`
  - `frontend/src/layout/MerchantLayout.vue`
  - `frontend/src/i18n/locales/zh-CN.ts`
  - `frontend/src/i18n/locales/en-US.ts`

### 5. 商家商品页审核状态与上架按钮约束
- 目标：审核状态可视化；未审核通过时上架按钮禁用并提示原因。
- 验收：
  - 列表展示审核状态标签（待审核/通过/拒绝）。
  - `auditStatus != 1` 时上架按钮禁用，提示文案明确。
- 涉及文件：
  - `frontend/src/views/merchant/ProductListView.vue`
  - `frontend/src/utils/i18nStatus.ts`
  - `frontend/src/i18n/locales/zh-CN.ts`
  - `frontend/src/i18n/locales/en-US.ts`

### 6. 后端关键流程测试补齐（审核上架、评价回复、退款分支）
- 目标：覆盖规格核心分支，减少回归风险。
- 验收：
  - 新增断言：审核前上架失败，审核后上架成功。
  - 新增断言：商家可回复评价。
  - 新增测试：退款申请后商家同意/拒绝分支均可通过，并断言订单状态。
- 涉及文件：
  - `src/test/java/com/helloworld/onlineshopping/modules/flow/BackendSpecFlowIntegrationTest.java`

## P1（建议完成）

### 7. RAG 会话列表能力
- 目标：补齐“按会话查看AI历史”的入口能力。
- 验收：
  - 新增接口 `/api/rag/session/list`，仅返回当前用户会话，按更新时间倒序。
  - `history` 接口增加会话归属校验，禁止跨用户读取。
- 涉及文件：
  - `src/main/java/com/helloworld/onlineshopping/modules/rag/controller/RagController.java`
  - `src/main/java/com/helloworld/onlineshopping/modules/rag/service/RagService.java`
  - `src/main/java/com/helloworld/onlineshopping/modules/rag/vo/ChatSessionVO.java`

## Later（V2增强，非本轮验收阻塞）

- MQ 驱动的订单事件全链路增强
- ES 进阶搜索（聚合、建议、高亮策略优化）
- 优惠券/物流/售后模块扩展
- 可观测性（Actuator + Prometheus + tracing）
- 全面安全加固（限流、脱敏、审计）
- 更高覆盖率测试与性能压测
