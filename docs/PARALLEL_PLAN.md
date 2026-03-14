# Parallel Worktree Development Plan

> 5 independent feature branches, can run in 5 Claude Code windows simultaneously.
> Zero conflict — each touches different packages under `modules/`.

---

## Quick Setup

```bash
cd /Users/mac/Desktop/project/online-shopping

# Create all feature branches + worktrees in one shot
git branch feat/favorite-behavior
git branch feat/recommendation
git branch feat/rag-qa
git branch feat/ai-copywriting
git branch feat/agent-shopping

git worktree add ../online-shopping-wt-favorite   feat/favorite-behavior
git worktree add ../online-shopping-wt-recommend   feat/recommendation
git worktree add ../online-shopping-wt-rag         feat/rag-qa
git worktree add ../online-shopping-wt-ai-copy     feat/ai-copywriting
git worktree add ../online-shopping-wt-agent       feat/agent-shopping
```

Then open 5 terminal windows, each `cd` into one worktree, run `claude`.

---

## Worktree A: `feat/favorite-behavior`

**Scope**: Favorites, Likes, Browse History, Search History
**Touches**: `modules/favorite/`, `modules/behavior/`
**Tables used**: `user_favorite`, `user_browse_history` (already in schema.sql)
**Estimated files**: ~15
**Conflict risk**: NONE

### Prompt to paste into Claude:

```
You are working on the online-shopping Spring Boot project.
Base package: com.helloworld.onlineshopping
The project already has P0 modules (auth, user, merchant, product, cart, address, order, review, admin) fully implemented.

Your task: Implement the Favorite & User Behavior module (P1).

Requirements:
1. modules/favorite/ — User Favorite (toggle favorite on SPU)
   - Entity: UserFavoriteEntity (table: user_favorite, already exists in schema.sql)
   - DTO: FavoriteToggleDTO (spuId)
   - VO: FavoriteVO (spuId, title, mainImage, price, shopName)
   - Service: FavoriteService
     - toggleFavorite(spuId) — add if not exists, remove if exists
     - getFavoriteList(pageNum, pageSize) — paginated
     - isFavorited(spuId) — check status
   - Controller: FavoriteController at /api/favorite
     - POST /api/favorite/toggle
     - GET /api/favorite/list
     - GET /api/favorite/check/{spuId}
   - When toggling, also update product_spu.favorite_count (+1 or -1)

2. modules/behavior/ — Browse History
   - Entity: UserBrowseHistoryEntity (table: user_browse_history, already exists)
   - VO: BrowseHistoryVO (spuId, title, mainImage, price, browseTime)
   - Service: BrowseHistoryService
     - recordBrowse(spuId) — called from ProductService.getProductDetail()
     - getBrowseHistory(pageNum, pageSize) — paginated, latest first
     - clearHistory()
   - Controller: BrowseHistoryController at /api/behavior/history
     - GET /api/behavior/history
     - DELETE /api/behavior/history

3. Integration:
   - Modify ProductService.getProductDetail() to call BrowseHistoryService.recordBrowse()
   - Ensure browse_count on product_spu is incremented (already done, verify)

4. After writing code, run `./mvnw compile` to verify.
5. Commit with: feat(favorite): add user favorite toggle and browse history tracking

Do NOT create new tables — they already exist in schema.sql.
Do NOT modify any files outside modules/favorite/, modules/behavior/, and the single integration point in ProductService.
```

---

## Worktree B: `feat/recommendation`

**Scope**: Hot Products, Simple Weighted Recommendation
**Touches**: `modules/recommendation/`
**Tables used**: reads from `product_spu`, `user_browse_history`, `user_favorite`, `order_item`
**Estimated files**: ~8
**Conflict risk**: NONE

### Prompt to paste into Claude:

```
You are working on the online-shopping Spring Boot project.
Base package: com.helloworld.onlineshopping
The project already has P0 modules fully implemented.

Your task: Implement the Recommendation module (P1).

Requirements:
1. modules/recommendation/ — Product Recommendation
   - VO: RecommendProductVO (spuId, title, mainImage, minPrice, salesCount, score, reason)
   - Service: RecommendService with these strategies:
     a) getHotProducts(limit) — Redis ZSet "hot:products"
        - Score formula: browseCount*1 + likeCount*2 + favoriteCount*3 + salesCount*5
        - Refresh via scheduled task every 10 minutes
        - Read from Redis, fallback to DB query
     b) getSimilarProducts(spuId, limit) — same category, exclude self, order by salesCount
     c) getPersonalRecommend(userId, limit) — based on user's browse/favorite/order history
        - Find categories user interacted with most
        - Recommend top products from those categories that user hasn't bought
   - Controller: RecommendController at /api/recommend
     - GET /api/recommend/hot?limit=10 (public)
     - GET /api/recommend/similar/{spuId}?limit=6 (public)
     - GET /api/recommend/personal?limit=10 (authenticated)
   - Scheduler: HotProductScheduler
     - @Scheduled(fixedRate = 600000) — every 10 min
     - Compute scores for all on-shelf SPUs, write to Redis ZSet "hot:products"

2. Redis Key Design:
   - hot:products — ZSet, member=spuId, score=热度值
   - recommend:personal:{userId} — String/List, cached 30 min

3. After writing code, run `./mvnw compile` to verify.
4. Commit with: feat(recommend): add hot products, similar, and personal recommendation

Do NOT modify any existing files. Only create new files under modules/recommendation/.
```

---

## Worktree C: `feat/rag-qa`

**Scope**: RAG Product Q&A, AI Chat Session
**Touches**: `modules/rag/`
**Tables used**: `product_knowledge_doc`, `ai_chat_session`, `ai_chat_message` (all in schema.sql)
**Estimated files**: ~12
**Conflict risk**: NONE

### Prompt to paste into Claude:

```
You are working on the online-shopping Spring Boot project.
Base package: com.helloworld.onlineshopping
The project already has P0 modules fully implemented.

Your task: Implement the RAG Product Q&A module (P2 — simplified version).

Requirements:
1. modules/rag/entity/ — Use existing tables
   - ProductKnowledgeDocEntity (table: product_knowledge_doc)
   - AiChatSessionEntity (table: ai_chat_session)
   - AiChatMessageEntity (table: ai_chat_message)

2. modules/rag/service/KnowledgeService
   - importFromProduct(spuId) — auto-generate knowledge docs from SPU title + detail + SKU specs
   - searchRelevant(spuId, question, limit) — simple keyword LIKE search in knowledge docs
     (no vector DB needed — just MySQL LIKE for course design)
   - addDoc(spuId, title, content, sourceType)

3. modules/rag/service/RagService
   - ask(spuId, question, sessionId?) — main Q&A entry point
     Step 1: Search relevant knowledge docs
     Step 2: Build prompt with context: "Based on the following product information: {docs}... Answer the user's question: {question}"
     Step 3: Call AI API (create an AiClient interface + a mock implementation that returns template answers)
     Step 4: Save to ai_chat_session / ai_chat_message
     Step 5: Return answer
   - getChatHistory(sessionId)

4. modules/rag/service/AiClient — Interface
   - String chat(String systemPrompt, String userMessage)
   - MockAiClient — @Profile("default") implementation that returns formatted template responses
     (so the project runs without real API key)
   - RealAiClient — @Profile("ai") implementation placeholder that calls OpenAI-compatible API
     (read api-key and base-url from application.yml, use RestTemplate)

5. modules/rag/controller/RagController at /api/rag
   - POST /api/rag/ask — {spuId, question, sessionId?} → RagAnswerVO
   - GET /api/rag/session/{sessionId}/history — chat history
   - POST /api/rag/knowledge/import/{spuId} — (MERCHANT) import product knowledge

6. DTO/VO:
   - RagAskDTO (spuId, question, sessionId)
   - RagAnswerVO (question, answer, sessionId, referenceDocTitles)
   - ChatMessageVO (role, content, createTime)

7. Add to application.yml (but commented out):
   # ai:
   #   api-key: sk-xxx
   #   base-url: https://api.openai.com/v1
   #   model: gpt-3.5-turbo

8. After writing code, run `./mvnw compile` to verify.
9. Commit with: feat(rag): add RAG product Q&A with knowledge base and mock AI client

Do NOT modify any existing files outside modules/rag/ (except adding config to application.yml).
```

---

## Worktree D: `feat/ai-copywriting`

**Scope**: AI Product Copywriting, AI Review Summary
**Touches**: `modules/ai/`
**Tables used**: reads from `product_spu`, `product_sku`, `product_review`
**Estimated files**: ~10
**Conflict risk**: NONE (separate from rag module)

### Prompt to paste into Claude:

```
You are working on the online-shopping Spring Boot project.
Base package: com.helloworld.onlineshopping
The project already has P0 modules fully implemented.

Your task: Implement AI Copywriting and AI Review Summary module (P2 — simplified).

Requirements:
1. modules/ai/service/AiClient — same interface as RAG module
   - String chat(String systemPrompt, String userMessage)
   - Create a MockAiClient that generates template-based responses
   - Make it a standalone interface (or reuse from rag if merged later)

2. modules/ai/service/CopywritingService
   - generateTitle(spuId) — generate product title variants
   - generateDescription(keywords, targetAudience, style) — generate marketing copy
   - generateSellingPoints(spuId) — extract and enhance selling points from product info
   Implementation: Build structured prompts from product data → call AiClient → return results
   MockAiClient should return realistic-looking template responses.

3. modules/ai/service/ReviewSummaryService
   - summarizeReviews(spuId) — analyze all reviews for a product
     Step 1: Fetch all reviews for the SPU
     Step 2: Build prompt: "Summarize these product reviews, extract top pros and cons: {reviews}"
     Step 3: Call AiClient
     Step 4: Return structured summary
   - Output: ReviewSummaryVO { spuId, totalReviews, averageScore, prosKeywords[], consKeywords[], summary }

4. modules/ai/controller/AiController at /api/ai
   - POST /api/ai/copywriting/title/{spuId} → CopywritingResultVO (MERCHANT)
   - POST /api/ai/copywriting/description → CopywritingResultVO (MERCHANT)
     Body: { keywords, targetAudience, style }
   - POST /api/ai/copywriting/selling-points/{spuId} → CopywritingResultVO (MERCHANT)
   - GET /api/ai/review-summary/{spuId} → ReviewSummaryVO (public)

5. DTO/VO:
   - CopywritingRequestDTO (keywords, targetAudience, style)
   - CopywritingResultVO (content, variants[])
   - ReviewSummaryVO (spuId, totalReviews, averageScore, pros[], cons[], summary)

6. After writing code, run `./mvnw compile` to verify.
7. Commit with: feat(ai): add AI copywriting generation and review summary

Do NOT modify any existing files. Only create new files under modules/ai/.
```

---

## Worktree E: `feat/agent-shopping`

**Scope**: Agent Shopping (weak agent), Planned Shopping
**Touches**: `modules/agent/`, `modules/plan/`
**Tables used**: `agent_task`, `shopping_plan`, `shopping_plan_item` (all in schema.sql)
**Estimated files**: ~16
**Conflict risk**: NONE

### Prompt to paste into Claude:

```
You are working on the online-shopping Spring Boot project.
Base package: com.helloworld.onlineshopping
The project already has P0 modules fully implemented.

Your task: Implement Agent Shopping and Planned Shopping modules (P2 — simplified weak-agent).

Requirements:

### Part 1: Agent Shopping (modules/agent/)

1. Entities:
   - AgentTaskEntity (table: agent_task, exists in schema.sql)

2. Service: AgentService — weak agent, does NOT auto-pay
   - createTask(userPrompt) — parse user intent (budget, category, preferences)
     Step 1: Save task with status=CREATED
     Step 2: Parse intent (simple keyword extraction, no NLP needed)
     Step 3: Search matching products via ProductService/ProductSpuMapper
     Step 4: Rank by relevance (price within budget, category match, sales)
     Step 5: Generate recommendation as JSON result
     Step 6: Update task status=COMPLETED, save result_json
   - getTaskResult(taskId) — return task with parsed result
   - confirmAndAddToCart(taskId, selectedSkuIds) — add recommended items to cart

3. Controller: AgentController at /api/agent
   - POST /api/agent/task — { userPrompt } e.g. "I want a laptop under 5000 yuan for coding"
   - GET /api/agent/task/{taskId}
   - POST /api/agent/task/{taskId}/add-to-cart — { skuIds }

4. VO:
   - AgentTaskVO (taskId, taskType, taskStatus, userPrompt, recommendations[])
   - AgentRecommendationVO (spuId, title, mainImage, price, matchReason)

### Part 2: Planned Shopping (modules/plan/)

5. Entities:
   - ShoppingPlanEntity (table: shopping_plan)
   - ShoppingPlanItemEntity (table: shopping_plan_item)

6. Service: ShoppingPlanService
   - createPlan(dto) — create plan with items
   - getPlanList() — user's plans
   - getPlanDetail(planId)
   - cancelPlan(planId)
   - executePlan(planId) — match items to actual products, add to cart (NOT auto-order)
   - Scheduler: PlanReminderScheduler
     - @Scheduled(fixedRate = 60000) — check plans where trigger_time <= now and status = CREATED
     - Update status to REMINDED (in real system would send notification, here just log)

7. Controller: ShoppingPlanController at /api/plan
   - POST /api/plan — create plan
   - GET /api/plan/list
   - GET /api/plan/{planId}
   - POST /api/plan/{planId}/cancel
   - POST /api/plan/{planId}/execute — match products and add to cart

8. DTO:
   - ShoppingPlanCreateDTO (planName, triggerTime, budgetAmount, remark, items[])
   - ShoppingPlanItemDTO (keyword, categoryId, expectedPriceMin, expectedPriceMax, quantity)

9. VO:
   - ShoppingPlanVO (planId, planName, triggerTime, planStatus, budgetAmount, items[])
   - ShoppingPlanItemVO (keyword, categoryId, priceRange, quantity, matchedProduct?)

10. After writing code, run `./mvnw compile` to verify.
11. Commit with: feat(agent): add weak agent shopping and planned shopping with reminders

Do NOT modify any existing files. Only create new files under modules/agent/ and modules/plan/.
```

---

## Merge Strategy

After all worktrees complete:

```bash
cd /Users/mac/Desktop/project/online-shopping

# Merge one by one (no conflicts expected)
git merge feat/favorite-behavior
git merge feat/recommendation
git merge feat/rag-qa
git merge feat/ai-copywriting
git merge feat/agent-shopping

# Clean up worktrees
git worktree remove ../online-shopping-wt-favorite
git worktree remove ../online-shopping-wt-recommend
git worktree remove ../online-shopping-wt-rag
git worktree remove ../online-shopping-wt-ai-copy
git worktree remove ../online-shopping-wt-agent

# Final verification
./mvnw compile

# Tag the release
git tag -a v1.0.0 -m "release: complete P0+P1+P2 all modules"
```

---

## Dependency Graph (No Conflicts)

```
master (P0 core)
    │
    ├── feat/favorite-behavior   → modules/favorite/, modules/behavior/
    │                               reads: product_spu (no write conflict)
    │
    ├── feat/recommendation      → modules/recommendation/
    │                               reads: product_spu, order_item, user_favorite, user_browse_history
    │
    ├── feat/rag-qa              → modules/rag/
    │                               reads: product_spu, product_sku
    │                               writes: product_knowledge_doc, ai_chat_*
    │
    ├── feat/ai-copywriting      → modules/ai/
    │                               reads: product_spu, product_sku, product_review
    │
    └── feat/agent-shopping      → modules/agent/, modules/plan/
                                    reads: product_spu, product_sku
                                    writes: agent_task, shopping_plan*
```

All 5 branches write to **completely different packages and tables**. Zero merge conflict guaranteed.
