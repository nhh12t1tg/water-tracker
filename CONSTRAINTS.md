# Constraints: Water Tracker Project

Last reviewed: 2026-11-18 by Product Architecture Team

## Floor (Always Enforced Across All Agents)

- **FROZEN CONTRACT INTEGRITY:** File `types/water.ts` is strictly READ-ONLY. No agent may modify, patch, or delete this file.
- **ZERO OVERLAP DIRECTORY BOUNDARY:** Each agent has exclusive write access ONLY to its assigned directories. Any commit modifying files outside an agent's assigned directories will be rejected automatically during review.
- **ADMOB SAFETY MANDATE:** 100% of dev and test code must use Google Official Sample Test Ad Unit IDs (`ca-app-pub-3940256099942544/...`). Zero live ad IDs.
- **INTERSTITIAL COOLDOWN:** Enforce minimum 3-minute (180,000ms) cooldown on Interstitial Ads.
- **QUIET HOURS (DND):** Notifications strictly suppressed between 22:00 and 07:00 (or user-customized bedtime).
- **NO SUPPRESSIONS:** No `@ts-ignore`, `eslint-disable`, empty `catch {}`, or unimplemented stubs.

## 5-Agent Ownership Matrix

| Agent | Git Branch | Working Path | Exclusive Directory Ownership | Primary Deliverables |
| :--- | :--- | :--- | :--- | :--- |
| **Agent 1** [Core & Storage] | `feature/agent-1-storage` | `.worktrees/agent-1-storage` | `services/storage/`<br>`features/onboarding/` | Local SQLite/Room DB, UserProfile persistence, Onboarding water calculation screen |
| **Agent 2** [UI Tracker] | `feature/agent-2-tracker` | `.worktrees/agent-2-tracker` | `features/tracker/` | Liquid Wave Canvas animation, percentage progress, Quick-log 150ml/250ml/500ml pills |
| **Agent 3** [Notification & Settings] | `feature/agent-3-notification` | `.worktrees/agent-3-notification` | `services/notification/`<br>`features/settings/` | Background Worker reminder notifications, Settings screen (wake/sleep hours, DND toggle) |
| **Agent 4** [AdMob Specialist] | `feature/agent-4-admob` | `.worktrees/agent-4-admob` | `config/admob.config.ts`<br>`services/admob/`<br>`components/ads/` | Google Mobile Ads SDK facade, Adaptive Banner (60dp safe height), 3-min Interstitial cooldown, Rewarded Ad |
| **Agent 5** [History & QA] | `feature/agent-5-history-qa` | `.worktrees/agent-5-history-qa` | `features/history/`<br>`tests/` | 7-day Weekly history chart, Streak tracker, comprehensive Unit & Integration test suite |

## Enforced Quality Gates

| Dimension | Rule | Checked by | Runs at |
| :--- | :--- | :--- | :--- |
| **Contract Immutability** | `types/water.ts` diff == 0 | `git diff --name-only main...HEAD \| grep -q "types/water.ts" && exit 1 \|\| exit 0` | pre-commit, PR |
| **Directory Isolation** | Agent only touches assigned path | Verification script per agent worktree | pre-commit, PR |
| **Types & Lint** | Zero type errors | `tsc --noEmit` | every commit |
| **AdMob Unit ID Safety** | No real pub IDs in test/dev | `grep -r "ca-app-pub-" src/ \| grep -v "3940256099942544"` (must be empty) | task end, CI |
| **Test Coverage** | Unit test suite 100% pass | `npm test` | task end, CI |
| **Safe Area & Padding** | Adaptive Banner height 60dp with >=16dp padding | Visual & layout inspection | review |
