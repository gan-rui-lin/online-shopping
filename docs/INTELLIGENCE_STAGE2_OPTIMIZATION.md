# 智能中心第二阶段优化

## 1. 现状问题

- RAG 检索链路对口语化问题（如“这个/那款”）识别弱，跨轮次语境利用不足。
- 知识导入在多次触发时可能重复写入，导致文档膨胀与检索噪声增加。
- RAG 回答缺少稳定的来源展示，结果可追溯性不足。
- Agent 意向任务把大量候选直接交给 AI，成本与时延偏高。
- AI 返回 JSON 时常混入 markdown code fence，导致解析偶发失败。
- AI 客户端配置中的 `timeout-ms` 未生效，异常时恢复速度慢。

## 2. 优化目标

- 提升 RAG 回答准确性与可解释性，补齐引用与拒答机制。
- 降低重复 AI 调用和无效候选筛选造成的资源消耗。
- 提升智能中心关键链路稳定性（AI 输出容错、请求超时、缓存命中）。

## 3. 技术方案

### 3.1 RAG

- 查询增强：当问题含“这个/那款/it/this”等代词时，拼接最近用户问题作为检索查询。
- 检索改进：对知识文档做关键词打分排序（标题高权重、正文次权重），并保留回退策略。
- 知识导入幂等：对 `PRODUCT_DESC`/`SKU_SPEC` 先清理后重建，避免重复导入。
- 语义缓存：同用户、同商品集合、同问题与同上下文短期命中缓存，减少模型调用。
- 引用与拒答：无证据时明确拒答；有证据时追加参考来源列表。

### 3.2 Agent

- 本地预筛：先按需求关键词对候选 SPU 打分，截断到高相关子集后再交给 AI 精筛。
- 解析鲁棒：AI JSON 解析前统一去除 code fence 与非 JSON 包装文本。
- 加购稳健性：SKU 入参去重与空校验，避免无效数据库写入。

### 3.3 AI 客户端

- 开关控制：支持 `app.ai.enabled=false` 快速降级。
- 超时生效：将 `app.ai.timeout-ms` 应用于连接与读取超时。

### 3.4 前端

- RAG 回答区展示参考来源，形成“答案 + 引用”闭环。

## 4. 已完成改动

- `src/main/java/com/helloworld/onlineshopping/modules/rag/service/RagService.java`
- `src/main/java/com/helloworld/onlineshopping/modules/rag/service/KnowledgeService.java`
- `src/main/java/com/helloworld/onlineshopping/modules/agent/service/AgentService.java`
- `src/main/java/com/helloworld/onlineshopping/common/utils/OpenAiCompatibleChatClient.java`
- `src/main/java/com/helloworld/onlineshopping/common/utils/AiJsonExtractor.java`
- `src/main/java/com/helloworld/onlineshopping/common/config/CaffeineConfig.java`
- `src/main/java/com/helloworld/onlineshopping/common/config/CacheConfig.java`
- `src/main/java/com/helloworld/onlineshopping/modules/ai/service/CopywritingService.java`
- `src/main/java/com/helloworld/onlineshopping/modules/ai/service/ReviewSummaryService.java`
- `frontend/src/views/buyer/IntelligenceHubView.vue`
- `frontend/src/i18n/locales/zh-CN.ts`
- `frontend/src/i18n/locales/en-US.ts`

## 5. 建议验收指标

- RAG 重复问题命中缓存后平均响应时间下降。
- RAG 答案中参考来源展示率达到 90% 以上（有检索文档时）。
- Agent 意向任务平均 AI 入参候选量下降，任务成功率不下降。
- AI JSON 解析异常率降低（重点观察 Agent 推荐与商品评估）。
