# Admin-Side Hardening Plan

## 1) Current Gap Analysis

### Findings from `todo.md` (relevant context)
- Current `todo.md` focuses on merchant-side flow completion (product audit constraints, merchant intelligence/review pages, and core process tests).
- It already mentions security hardening and auditing in the "Later" section, but lacks concrete admin-side execution items and iteration ownership.

### Admin-side gaps discovered in codebase
- Member management is missing (no admin user list, status control, or role visibility).
- Order intervention is missing (admin cannot search all orders or perform intervention operations).
- Dashboard visualization is weak (only aggregate counters, no trend / structure / health dimensions).
- Admin UI lacks task-oriented navigation and consistency for governance workflows.
- Operation logging is insufficient for admin behavior tracking (only order operation logs exist, not admin action logs).
- Security governance lacks admin-facing observability and anti-bruteforce control (no login rate-limit/lock feedback).

## 2) Targeted Improvement Scope

This plan will implement incremental, production-oriented improvements around:

1. Member Management
2. Order Intervention
3. Platform Data Visualization
4. UI Polish
5. Operation Logging
6. Security Management

## 3) Iteration Plan (Small, Frequent Commits)

### Iteration A: Docs and baseline planning
- Deliverables:
  - Add this `plan.md` with gap analysis and rollout phases.
  - Align with `todo.md` by explicitly adding admin governance backlog and acceptance intent.
- Commit type: `docs`

### Iteration B: Member management foundation
- Backend:
  - Add admin APIs for paginated member query with filters (`keyword`, `status`, `userType`).
  - Add member enable/disable operation with guardrails (cannot disable self).
- Frontend:
  - Add admin "Members" page for query/filter, role/user-type display, status switch.
  - Add admin menu and route entry.
- Commit types: `feat`, `fix`

### Iteration C: Order intervention capabilities
- Backend:
  - Add admin order list API (cross-shop visibility with status/user filters).
  - Add admin intervention actions (forced cancel, refund approve/reject) with state checks.
- Frontend:
  - Add admin "Orders" page for intervention operations and order state overview.
- Commit types: `feat`, `fix`

### Iteration D: Data visualization + UI polish
- Backend:
  - Expand dashboard response with 7-day order trend, 7-day GMV trend, status distribution.
- Frontend:
  - Upgrade admin dashboard to visual trend cards/charts-style panels.
  - Improve admin layout visual hierarchy and consistency.
- Commit types: `feat`, `style`, `refactor`

### Iteration E: Operation logging
- Backend:
  - Add `admin_action_log` entity/table + service + API.
  - Persist key admin operations (member status changes, order interventions, audit actions).
- Frontend:
  - Add admin operation log page with query capabilities.
- Commit types: `feat`

### Iteration F: Security hardening
- Backend:
  - Add login failure throttling/temporary lock.
  - Add security overview API (lock stats + safety baseline indicators).
- Frontend:
  - Add security management page for policy visibility and governance monitoring.
- Commit types: `feat`, `fix`

### Iteration G: verification and stabilization
- Add/adjust targeted tests where practical for new admin service logic.
- Run backend/frontend verification commands and fix regressions.
- Commit types: `test`, `chore`

## 4) Engineering Constraints

- Keep API-compatible changes additive to avoid breaking existing buyer/merchant paths.
- Apply explicit state validation for all intervention actions.
- Log all sensitive admin operations with actor + target + result + timestamp.
- Maintain small, reviewable commit size and independent rollback safety.
