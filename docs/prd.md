# Product Requirements Document (PRD): Water Tracker Mobile App

## 1. Executive Summary & Outcome
A lightweight, offline-first Android "Water Tracker" application built with Jetpack Compose / TypeScript bridge, focusing on health habit building, smart reminders, and compliant Google AdMob monetization.

## 2. Core Constraints & Formulas
- **Target Calculation Formula:** `Weight(kg) * (Gender === Male ? 35 : 31) + ActivityOffset(0, 350, 700)`. Supports manual goal override.
- **Beverages & Hydration Factors:**
  - Pure Water: `1.0` (100%)
  - Tea / Coffee: `0.8` (mild diuretic effect)
  - Milk / Juice: `0.9`
  - Electrolytes / Sports: `1.0`
- **AdMob Safety & Better Ads Standards:**
  - Adaptive Banner at screen bottom with ≥16dp touch padding to avoid accidental clicks.
  - Interstitial Ads only trigger at natural transition points (e.g. navigation to Weekly History tab or upon reaching 100% daily goal), strictly capped by 4-minute cooldown.
  - Rewarded Ads for explicit user perks: Unlocking Wave Animation Themes and Freezing/Restoring Broken Streaks.
  - Mandatory Google Test Ad Unit IDs during development.
- **Smart Notification Engine:**
  - Active between 07:00 and 22:00.
  - Absolute Do Not Disturb (DND) during sleeping hours.
  - 90–120 minute interval with smart suppression if the user logged water within the past 45–60 minutes.
  - Quick action `[+250ml]` directly on notification bar.

## 3. Parallel Agent Boundaries & Separation of Concerns
To enable multi-agent parallel development without git conflicts or race conditions:
1. `types/`: Shared frozen interfaces (`types/water.ts`). Read-only.
2. `config/`: Application and AdMob configuration (`config/admob.config.ts`).
3. `services/admob/`: Isolated AdMob Facade service (`services/admob/admob.service.ts`).
4. `services/storage/`: Offline-first database & persistent store.
5. `services/notification/`: Notification scheduler & smart suppress logic.
6. `features/onboarding/`: Profile calculation & target setup screen.
7. `features/tracker/`: Main dashboard, wave animation, one-touch drink logging.
8. `features/history/`: Daily/weekly logs, streak visualization, stats.
9. `features/settings/`: Quiet hours, cup sizes, theme selector.
