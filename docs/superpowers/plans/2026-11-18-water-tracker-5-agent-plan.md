# Water Tracker 5-Agent Parallel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver the complete offline-first Water Tracker application with liquid wave UI, smart notifications, local persistence, and compliant Google AdMob monetization by orchestrating 5 parallel agents across isolated git worktrees matching the Step 2 architectural floorplan with zero directory collisions.

**Architecture:** Domain-Driven Design (DDD) with clean unidirectional data flow. All 5 agents consume the immutable shared contract in `types/water.ts` without modifying it. Each agent owns a distinct set of directory boundaries and operates inside its dedicated Git worktree branch.

**Tech Stack:** TypeScript / Kotlin Jetpack Compose, Room / SQLite Local Storage, Android WorkManager / AlarmManager, Google Mobile Ads SDK (AdMob), Vitest / Jest.

**Spec:** [docs/prd.md](file:///home/nhh/Projects/app/docs/prd.md) and [CONSTRAINTS.md](file:///home/nhh/Projects/app/CONSTRAINTS.md)

---

## Global Constraints

- **FROZEN CONTRACT INTEGRITY:** File `types/water.ts` is strictly READ-ONLY. No agent may modify, patch, or delete this file.
- **ZERO OVERLAP DIRECTORY BOUNDARY:** Agents have exclusive write privileges to their assigned directories only:
  - Agent 1: `services/storage/` and `features/onboarding/`
  - Agent 2: `features/tracker/`
  - Agent 3: `services/notification/` and `features/settings/`
  - Agent 4: `config/admob.config.ts`, `services/admob/`, and `components/ads/`
  - Agent 5: `features/history/` and `tests/`
- **ADMOB SAFETY MANDATE:** 100% of dev and test code must use Google Official Sample Test Ad Unit IDs (`ca-app-pub-3940256099942544/...`). Zero live ad IDs.
- **INTERSTITIAL COOLDOWN:** Hardcoded minimum 3-minute (180,000ms) cooldown enforced on Interstitial Ads. Never trigger during active user interactions.
- **QUIET HOURS (DND):** Smart notification engine must never trigger alarms or notifications between 22:00 and 07:00.
- **ACCIDENTAL CLICK PREVENTION:** Adaptive Banner container must reserve 60dp height and keep >=16dp padding away from interactive buttons.

---

## Review Focus

1. **Boundary Weight Calculation & Negative Goal Prevention:** Onboarding inputs with edge-case weights (`weightKg <= 0` or `weightKg > 300`) must be sanitized to valid sane defaults (`50kg - 150kg`), preventing negative or zero target hydration calculations.
2. **Local DB Multi-Date Attribution:** Logs entered across midnight boundaries (23:59 vs 00:01) must strictly group by ISO date string (`YYYY-MM-DD`) matching the user's local timezone.
3. **Interstitial Rapid-Tap Spam:** Multiple rapid navigation events or goal completion dialog clicks within seconds must be safely gated by the 3-minute cooldown timer, returning `false` instead of displaying consecutive ads.
4. **Smart Notification Delay After Drink:** When a user logs a drink within 45 minutes of a scheduled reminder, the scheduler must reschedule the alarm by 90 minutes instead of firing immediately.
5. **Effective Volume Floating-Point Accuracy:** Calculating effective volume for beverages with fractional hydration factors (e.g. coffee factor `0.8` on `250ml`) must return integer `200ml` via `Math.round()` without precision drift (`199.99999999999997ml`).

---

## Worktree Assignment & Tasks

```
Repo Root: /home/nhh/Projects/app
├── .worktrees/agent-1-storage      -> branch: feature/agent-1-storage
├── .worktrees/agent-2-tracker      -> branch: feature/agent-2-tracker
├── .worktrees/agent-3-notification -> branch: feature/agent-3-notification
├── .worktrees/agent-4-admob        -> branch: feature/agent-4-admob
└── .worktrees/agent-5-history-qa   -> branch: feature/agent-5-history-qa
```

---

### Task 1: Agent 1 [Core & Storage]

**Working Path:** `.worktrees/agent-1-storage`  
**Branch:** `feature/agent-1-storage`  
**Exclusive Directories:** `services/storage/`, `features/onboarding/`

**Files:**
- Create: `services/storage/water-storage.service.ts`
- Create: `services/storage/sqlite-driver.ts`
- Create: `features/onboarding/screens/OnboardingScreen.tsx`
- Create: `features/onboarding/utils/calculator.ts`
- Create: `features/onboarding/index.ts`
- Test: `services/storage/__tests__/storage.test.ts`

**Interfaces:**
- Consumes: `types/water.ts` (`UserProfile`, `WaterLogEntry`, `DailyProgress`, `BeverageType`)
- Produces:
  - `calculateIntakeGoal(weightKg: number, gender: Gender, activity: ActivityLevel, customOverride?: number): number`
  - `WaterStorageService`:
    - `saveUserProfile(profile: UserProfile): Promise<void>`
    - `getUserProfile(): Promise<UserProfile | null>`
    - `logDrink(entry: WaterLogEntry): Promise<DailyProgress>`
    - `getDailyProgress(dateStr: string): Promise<DailyProgress>`
    - `getWeeklyProgress(endDateStr: string): Promise<DailyProgress[]>`
  - `OnboardingScreen({ onComplete: (profile: UserProfile) => void })`

- [ ] **Step 1: Write failing test for water calculation formula and storage CRUD**
- [ ] **Step 2: Verify test fails**
- [ ] **Step 3: Implement `calculateIntakeGoal`:** Formula `(weightKg * (gender === 'male' ? 35 : 31)) + offset` (sedentary: 0, moderate: 350, active: 700) or return `customOverride` if provided.
- [ ] **Step 4: Implement `WaterStorageService`:** Local SQLite/Room wrapper with transactions for logs, progress aggregation, and profile updates.
- [ ] **Step 5: Implement `OnboardingScreen`:** 3-step intake form (Gender -> Weight & Activity -> Confirmation with daily target).
- [ ] **Step 6: Run tests and verify 100% pass**
- [ ] **Step 7: Commit in worktree**
  ```bash
  git add services/storage/ features/onboarding/
  git commit -m "feat(storage-onboarding): implement local storage service and onboarding flow"
  ```

---

### Task 2: Agent 2 [UI Tracker]

**Working Path:** `.worktrees/agent-2-tracker`  
**Branch:** `feature/agent-2-tracker`  
**Exclusive Directories:** `features/tracker/`

**Files:**
- Create: `features/tracker/components/WaveProgress.tsx`
- Create: `features/tracker/components/QuickDrinkBar.tsx`
- Create: `features/tracker/screens/TrackerScreen.tsx`
- Create: `features/tracker/index.ts`
- Test: `features/tracker/__tests__/tracker.test.ts`

**Interfaces:**
- Consumes: `types/water.ts` (`DailyProgress`, `BeverageType`, `RewardRewardType`)
- Produces:
  - `WaveProgress({ progressPercentage: number, currentMl: number, targetMl: number, waveColor?: string })`
  - `QuickDrinkBar({ presets: number[], onQuickAdd: (ml: number) => void, onOpenThemeShop: () => void })`
  - `TrackerScreen({ progress: DailyProgress, onAddDrink: (ml: number) => void, onOpenThemeReward: () => void })`

- [ ] **Step 1: Write failing test for quick drink presets (150ml, 250ml, 500ml) and progress calculation**
- [ ] **Step 2: Verify test fails**
- [ ] **Step 3: Implement `WaveProgress`:** Dual-sine wave canvas animation with spring physics on water level transition and glassmorphism specular rim.
- [ ] **Step 4: Implement `QuickDrinkBar`:** 1-touch pills + pulsing Gift icon button (🎁) for unlocking theme colors.
- [ ] **Step 5: Implement `TrackerScreen`:** Coordinates wave visualizer, controls, motivation subtitle, and reserves 60dp bottom slot for Adaptive Banner.
- [ ] **Step 6: Run tests and verify 100% pass**
- [ ] **Step 7: Commit in worktree**
  ```bash
  git add features/tracker/
  git commit -m "feat(tracker): implement wave animation, quick drink bar and tracker screen"
  ```

---

### Task 3: Agent 3 [Notification & Settings]

**Working Path:** `.worktrees/agent-3-notification`  
**Branch:** `feature/agent-3-notification`  
**Exclusive Directories:** `services/notification/`, `features/settings/`

**Files:**
- Create: `services/notification/notification.service.ts`
- Create: `services/notification/dnd-helper.ts`
- Create: `features/settings/screens/SettingsScreen.tsx`
- Create: `features/settings/components/QuietHoursPicker.tsx`
- Create: `features/settings/index.ts`
- Test: `services/notification/__tests__/notification.test.ts`

**Interfaces:**
- Consumes: `types/water.ts` (`UserProfile`, `WaterLogEntry`)
- Produces:
  - `NotificationService`:
    - `isQuietHours(timeStr: string, wakeUp: string, bed: string): boolean`
    - `calculateNextScheduleTime(lastDrinkTime: string | null, intervalMinutes: number, wakeUp: string, bed: string): Date | null`
    - `scheduleNextReminder(profile: UserProfile, lastDrink?: WaterLogEntry): Promise<void>`
  - `SettingsScreen({ profile: UserProfile, onUpdateProfile: (updated: UserProfile) => void })`

- [ ] **Step 1: Write failing test for DND quiet hours (22:00 -> 07:00) and 45-minute drink suppression delay**
- [ ] **Step 2: Verify test fails**
- [ ] **Step 3: Implement `dnd-helper.ts`:** Circular midnight boundary checks for quiet hours.
- [ ] **Step 4: Implement `notification.service.ts`:** Schedules reminders at configured intervals; delays next notification by 90 minutes if a drink was logged recently; attaches `[+250ml Nước]` quick-action payload.
- [ ] **Step 5: Implement `SettingsScreen`:** Configures bedtime, wake-up time, reminder interval, and sound toggles.
- [ ] **Step 6: Run tests and verify 100% pass**
- [ ] **Step 7: Commit in worktree**
  ```bash
  git add services/notification/ features/settings/
  git commit -m "feat(notification-settings): implement smart notification scheduler and settings screen"
  ```

---

### Task 4: Agent 4 [AdMob Specialist]

**Working Path:** `.worktrees/agent-4-admob`  
**Branch:** `feature/agent-4-admob`  
**Exclusive Directories:** `config/admob.config.ts`, `services/admob/`, `components/ads/`

**Files:**
- Modify: `config/admob.config.ts` (enforce test unit IDs and 180,000ms cooldown)
- Modify/Create: `services/admob/admob.service.ts`
- Create: `components/ads/AdaptiveBannerAd.tsx`
- Create: `components/ads/index.ts`
- Test: `services/admob/__tests__/admob.test.ts`

**Interfaces:**
- Consumes: `types/water.ts` (`IAdMobService`, `AdMobConfig`, `RewardRewardType`)
- Produces:
  - `AdMobService` singleton implementing `IAdMobService`
  - `canShowInterstitial(): boolean` (strictly enforcing 3-min / 180,000ms cooldown)
  - `showInterstitial(onDismissed, onError): boolean`
  - `showRewarded(type, onGranted, onDismissed, onError): void`
  - `AdaptiveBannerAd({ isTest?: boolean })`: 60dp height with >=16dp padding to avoid accidental clicks

- [ ] **Step 1: Write failing test asserting Google Test IDs and 3-minute cooldown enforcement**
- [ ] **Step 2: Verify test fails**
- [ ] **Step 3: Configure `config/admob.config.ts`:** Set `interstitialCooldownMs = 180000` and official test unit IDs.
- [ ] **Step 4: Implement `services/admob/admob.service.ts`:** Full lifecycle with preloading, frequency cap timer, and rewarded callback handler.
- [ ] **Step 5: Implement `components/ads/AdaptiveBannerAd.tsx`:** Container with reserved 60dp layout space and safety margins.
- [ ] **Step 6: Run tests and verify 100% pass**
- [ ] **Step 7: Commit in worktree**
  ```bash
  git add config/admob.config.ts services/admob/ components/ads/
  git commit -m "feat(admob): implement AdMob service with 3-min cooldown and safe adaptive banner"
  ```

---

### Task 5: Agent 5 [History & QA]

**Working Path:** `.worktrees/agent-5-history-qa`  
**Branch:** `feature/agent-5-history-qa`  
**Exclusive Directories:** `features/history/`, `tests/`

**Files:**
- Create: `features/history/screens/HistoryScreen.tsx`
- Create: `features/history/components/WeeklyChart.tsx`
- Create: `features/history/components/StreakBadges.tsx`
- Create: `features/history/index.ts`
- Create: `tests/unit/hydration-core.test.ts`
- Create: `tests/unit/admob-policy.test.ts`
- Create: `tests/unit/notification-dnd.test.ts`
- Create: `tests/integration/end-to-end-flow.test.ts`

**Interfaces:**
- Consumes:
  - `types/water.ts`
  - `services/storage/water-storage.service.ts`
  - `services/admob/admob.service.ts`
- Produces:
  - `HistoryScreen({ weeklyLogs: DailyProgress[], streakState: StreakState, onRestoreStreak: () => void })`
  - Comprehensive Unit and Integration Test Suite verifying all 5 Review Focus edge cases

- [ ] **Step 1: Write failing tests for 7-day chart rendering and streak milestone thresholds (3, 7, 14, 30 days)**
- [ ] **Step 2: Verify test fails**
- [ ] **Step 3: Implement `WeeklyChart`:** Glassmorphic bar chart comparing daily intake vs target line.
- [ ] **Step 4: Implement `StreakBadges`:** Flame badges and "Cứu Streak" button linked to Rewarded Ad.
- [ ] **Step 5: Write comprehensive test suites:**
  - `tests/unit/hydration-core.test.ts`: Formula boundaries, beverage factors.
  - `tests/unit/admob-policy.test.ts`: Test unit IDs verification, 3-min cooldown checks.
  - `tests/unit/notification-dnd.test.ts`: DND suppression and smart rescheduling.
  - `tests/integration/end-to-end-flow.test.ts`: Complete user journey from onboarding to drink log to streak update.
- [ ] **Step 6: Run full test suite across all modules and confirm 100% green**
- [ ] **Step 7: Commit in worktree**
  ```bash
  git add features/history/ tests/
  git commit -m "feat(history-qa): implement weekly history screen, streak badges and full test suite"
  ```
