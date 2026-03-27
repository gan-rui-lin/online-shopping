# Smart Intelligence Hub Overview

## Purpose
The Smart Intelligence Hub provides three AI-assisted capabilities for buyers:
1. RAG-based product Q&A
2. Agent-assisted purchasing workflows
3. AI copywriting and product evaluation

It is designed to work with user-owned product context (favorites and browse history) and produce actionable outputs in a single interface.

## Core Capabilities

### 1) RAG Product Q&A
- Users select one product from favorites or browsing history.
- Users ask a natural-language question.
- Backend performs retrieval + generation and returns an answer with session context.
- Frontend keeps chat-like history for continuity.

### 2) Agent Purchasing
Two task modes are supported:
- NECESSITY (Scheduled Refill):
  - User chooses category, frequency, quantity, and a bound product.
  - System creates a shopping plan directly (no cart action required).
- INTENTION (Intent Recommendation):
  - User provides product name + preference requirements (+ optional budget).
  - System generates recommended SKUs.
  - User can select SKUs and add them to cart.

### 3) AI Copywriting + Evaluation
- Users select a product card from favorites or browsing history.
- System generates:
  - Title variants
  - Description
  - Selling points
- System also returns:
  - Review summary
  - Multi-dimensional evaluation (quality, value, scenario fit, risks, conclusion)

## Backend API Surface (High Level)
- RAG:
  - `POST /api/rag/ask`
- Agent:
  - `POST /api/agent/tasks`
  - `POST /api/agent/tasks/{taskId}/cart`
- AI:
  - `POST /api/ai/copywriting/title/{spuId}`
  - `POST /api/ai/copywriting/description`
  - `POST /api/ai/copywriting/selling-points/{spuId}`
  - `GET /api/ai/review-summary/{spuId}`
  - `GET /api/ai/copywriting/evaluate/{spuId}`

## Frontend Interaction Rules
- Product selection in preview lists uses a unique UI key per card to avoid multi-select highlight on duplicated history items.
- Review summaries can contain Markdown-like text and should be rendered as formatted content.
- AI generation requests should not block successful partial results when one sub-call fails.

## Security and Access
- Endpoints are protected by authentication unless explicitly public in security config.
- Copywriting endpoints are intended to be available to authenticated buyers in the intelligence hub flow.

## Operational Notes
- AI integration is OpenAI-compatible and configurable via application settings.
- Frontend request timeout is tuned for long AI calls.
- Long IDs used by Agent tasks are handled as strings in frontend payloads to avoid JavaScript precision issues.

## Known UX Expectations
- NECESSITY mode action should read as "Add to Scheduled Plan".
- INTENTION mode keeps "Run Task" + optional "Add to Cart" behavior.
- Copywriting tab should use preview-based product selection, not manual keyword-heavy forms.
