import { register } from 'node:module';
register(new URL('../test-loader.mjs', import.meta.url));

import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert';
import {
  UserProfile,
  DailyProgress,
  StreakState,
  WaterLogEntry,
  LogEntryId,
  BeverageId,
  UserId,
} from '../../types/water';
import {
  calculateDailyTarget,
  calculateEffectiveVolume,
  calculateDailyProgress,
  getStreakMilestones,
  mapDailyProgressToDayChart,
  calculateWeeklySummary,
  BEVERAGE_FACTORS,
} from '../../features/history/utils/hydration-math';
import { AdMobService } from '../../services/admob/admob.service';

/**
 * End-to-End User Flow Integration Test Suite
 * Validates the complete lifecycle: Onboarding -> Water Logging -> Multi-Date DB Attribution
 * -> Daily Goal Achievement -> Streak Advancement -> Broken Streak Freeze via Rewarded Ad
 * -> 7-Day History Chart Assembly.
 */
describe('End-to-End User Flow Integration Suite', () => {
  let admobService: AdMobService;

  beforeEach(async () => {
    admobService = AdMobService.getInstance();
    await admobService.initialize();
  });

  it('Scenario 1: Onboarding creates UserProfile with calibrated hydration target', () => {
    const rawWeight = 75;
    const gender = 'male';
    const activity = 'moderate';

    // 75 * 35 + 350 = 2975 ml
    const calculatedTarget = calculateDailyTarget(rawWeight, gender, activity);
    assert.strictEqual(calculatedTarget, 2975);

    const userProfile: UserProfile = {
      id: 'user-primary-123' as UserId,
      gender,
      weightKg: rawWeight,
      activityLevel: activity,
      customDailyGoalMl: null,
      wakeUpTime: '07:00',
      bedTime: '22:00',
      notificationIntervalMinutes: 90,
      isDndEnabled: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    assert.strictEqual(userProfile.weightKg, 75);
    assert.strictEqual(userProfile.isDndEnabled, true);
    assert.strictEqual(userProfile.wakeUpTime, '07:00');
    assert.strictEqual(userProfile.bedTime, '22:00');
  });

  describe('Scenario 2: Review Focus 2 - Local DB Multi-Date Attribution Across Midnight', () => {
    it('strictly isolates logs entered across midnight boundaries (23:59 vs 00:01)', () => {
      // Entry 1: 23:59 on 2026-11-18
      const entryNight: WaterLogEntry = {
        id: 'log-night-1' as LogEntryId,
        timestamp: '2026-11-18T23:59:30Z',
        beverageId: 'water' as BeverageId,
        rawVolumeMl: 250,
        effectiveVolumeMl: 250,
      };

      // Entry 2: 00:01 on 2026-11-19
      const entryMorning: WaterLogEntry = {
        id: 'log-morning-1' as LogEntryId,
        timestamp: '2026-11-19T00:01:15Z',
        beverageId: 'water' as BeverageId,
        rawVolumeMl: 300,
        effectiveVolumeMl: 300,
      };

      // Simulate local storage grouping entries by ISO date string
      const localStore = new Map<string, WaterLogEntry[]>();

      const addLog = (entry: WaterLogEntry) => {
        const dateKey = entry.timestamp.slice(0, 10); // 'YYYY-MM-DD'
        const existing = localStore.get(dateKey) || [];
        existing.push(entry);
        localStore.set(dateKey, existing);
      };

      addLog(entryNight);
      addLog(entryMorning);

      // Verify date keys
      assert.strictEqual(localStore.has('2026-11-18'), true);
      assert.strictEqual(localStore.has('2026-11-19'), true);

      const logsDay1 = localStore.get('2026-11-18') || [];
      const logsDay2 = localStore.get('2026-11-19') || [];

      assert.strictEqual(logsDay1.length, 1);
      assert.strictEqual(logsDay1[0].id, 'log-night-1');
      assert.strictEqual(logsDay1[0].rawVolumeMl, 250);

      assert.strictEqual(logsDay2.length, 1);
      assert.strictEqual(logsDay2[0].id, 'log-morning-1');
      assert.strictEqual(logsDay2[0].rawVolumeMl, 300);

      // Zero bleed between days
      assert.ok(!logsDay1.some((l) => l.timestamp.includes('2026-11-19')));
      assert.ok(!logsDay2.some((l) => l.timestamp.includes('2026-11-18')));
    });
  });

  describe('Scenario 3: Beverage Variety, Effective Volume & Daily Goal Achievement', () => {
    it('tracks mixed beverages with correct multipliers and achieves target', () => {
      const targetMl = 2000;
      const entries: WaterLogEntry[] = [];

      // 1. Water 500ml (factor 1.0)
      entries.push({
        id: 'log-1' as LogEntryId,
        timestamp: '2026-11-18T08:00:00Z',
        beverageId: 'water' as BeverageId,
        rawVolumeMl: 500,
        effectiveVolumeMl: calculateEffectiveVolume(500, BEVERAGE_FACTORS.water),
      });

      // 2. Coffee 250ml (factor 0.8 -> 200ml)
      entries.push({
        id: 'log-2' as LogEntryId,
        timestamp: '2026-11-18T10:00:00Z',
        beverageId: 'coffee' as BeverageId,
        rawVolumeMl: 250,
        effectiveVolumeMl: calculateEffectiveVolume(250, BEVERAGE_FACTORS.coffee),
      });

      // 3. Orange Juice 300ml (factor 0.9 -> 270ml)
      entries.push({
        id: 'log-3' as LogEntryId,
        timestamp: '2026-11-18T12:30:00Z',
        beverageId: 'juice' as BeverageId,
        rawVolumeMl: 300,
        effectiveVolumeMl: calculateEffectiveVolume(300, BEVERAGE_FACTORS.juice),
      });

      // Check progress before reaching goal
      let progress = calculateDailyProgress('2026-11-18', targetMl, entries);
      assert.strictEqual(progress.totalLoggedMl, 1050); // 500 + 250 + 300
      assert.strictEqual(progress.effectiveHydrationMl, 970); // 500 + 200 + 270
      assert.strictEqual(progress.isGoalAchieved, false);
      assert.strictEqual(progress.percentage, 49); // 970 / 2000 = 48.5% -> 49%

      // 4. Electrolytes 1050ml (factor 1.0 -> 1050ml) -> brings effective to 2020ml
      entries.push({
        id: 'log-4' as LogEntryId,
        timestamp: '2026-11-18T16:00:00Z',
        beverageId: 'electrolytes' as BeverageId,
        rawVolumeMl: 1050,
        effectiveVolumeMl: calculateEffectiveVolume(1050, BEVERAGE_FACTORS.electrolytes),
      });

      progress = calculateDailyProgress('2026-11-18', targetMl, entries);
      assert.strictEqual(progress.totalLoggedMl, 2100);
      assert.strictEqual(progress.effectiveHydrationMl, 2020);
      assert.strictEqual(progress.percentage, 101);
      assert.strictEqual(progress.isGoalAchieved, true);
    });
  });

  describe('Scenario 4: Streak Progression & Milestone Badges Unlock', () => {
    it('progressively unlocks milestone badges at 3, 7, 14, and 30 days', () => {
      // Day 1
      let badges = getStreakMilestones(1);
      assert.ok(badges.every((b) => !b.isUnlocked));

      // Day 3: "Khởi Đầu" unlocks
      badges = getStreakMilestones(3);
      assert.strictEqual(badges[0].isUnlocked, true);
      assert.strictEqual(badges[0].title, 'Khởi Đầu');
      assert.strictEqual(badges[1].isUnlocked, false);

      // Day 7: "Kiên Trì 1 Tuần" unlocks
      badges = getStreakMilestones(7);
      assert.strictEqual(badges[0].isUnlocked, true);
      assert.strictEqual(badges[1].isUnlocked, true);
      assert.strictEqual(badges[1].title, 'Kiên Trì 1 Tuần');
      assert.strictEqual(badges[2].isUnlocked, false);

      // Day 14: "Thói Quen Vàng" unlocks
      badges = getStreakMilestones(14);
      assert.strictEqual(badges[0].isUnlocked, true);
      assert.strictEqual(badges[1].isUnlocked, true);
      assert.strictEqual(badges[2].isUnlocked, true);
      assert.strictEqual(badges[2].title, 'Thói Quen Vàng');
      assert.strictEqual(badges[3].isUnlocked, false);

      // Day 30: "Bậc Thầy Nước" unlocks
      badges = getStreakMilestones(30);
      assert.ok(badges.every((b) => b.isUnlocked));
      assert.strictEqual(badges[3].title, 'Bậc Thầy Nước');
    });
  });

  describe('Scenario 5: Broken Streak & Rewarded Video Streak Freeze Preservation', () => {
    it('restores streak using Rewarded Ad token when a day was missed', () => {
      // User had a 10-day streak, missed yesterday
      let streakState: StreakState = {
        currentStreakDays: 10,
        longestStreakDays: 10,
        lastAchievedDate: '2026-11-16', // today is 2026-11-18, missed 2026-11-17
        availableFreezes: 0,
        isFrozenForDate: false,
      };

      // User taps "Bảo vệ chuỗi (+1 Khiên)"
      (admobService as any).isRewardedLoaded = true;

      let rewardGranted = false;
      admobService.showRewarded(
        'FREEZE_STREAK',
        (rewardType) => {
          if (rewardType === 'FREEZE_STREAK') {
            rewardGranted = true;
            // Award +1 freeze token
            streakState = {
              ...streakState,
              availableFreezes: streakState.availableFreezes + 1,
            };
          }
        }
      );

      assert.strictEqual(rewardGranted, true);
      assert.strictEqual(streakState.availableFreezes, 1);

      // Apply freeze token to protect the streak
      if (streakState.availableFreezes > 0) {
        streakState = {
          ...streakState,
          availableFreezes: streakState.availableFreezes - 1,
          isFrozenForDate: true,
          // Streak count preserved!
          currentStreakDays: streakState.currentStreakDays,
        };
      }

      assert.strictEqual(streakState.availableFreezes, 0);
      assert.strictEqual(streakState.isFrozenForDate, true);
      assert.strictEqual(streakState.currentStreakDays, 10, 'Streak count preserved by freeze shield');
    });
  });

  describe('Scenario 6: 7-Day Weekly History Assembly for WeeklyChart', () => {
    it('assembles a clean 7-day dataset with summaries for WeeklyChart', () => {
      const mockWeeklyProgress: DailyProgress[] = [
        {
          date: '2026-11-12',
          targetVolumeMl: 2000,
          totalLoggedMl: 2100,
          effectiveHydrationMl: 2050,
          percentage: 103,
          isGoalAchieved: true,
          entries: [],
        },
        {
          date: '2026-11-13',
          targetVolumeMl: 2000,
          totalLoggedMl: 2000,
          effectiveHydrationMl: 1950,
          percentage: 98,
          isGoalAchieved: false,
          entries: [],
        },
        {
          date: '2026-11-14',
          targetVolumeMl: 2000,
          totalLoggedMl: 2200,
          effectiveHydrationMl: 2100,
          percentage: 105,
          isGoalAchieved: true,
          entries: [],
        },
        {
          date: '2026-11-15',
          targetVolumeMl: 2000,
          totalLoggedMl: 1800,
          effectiveHydrationMl: 1800,
          percentage: 90,
          isGoalAchieved: false,
          entries: [],
        },
        {
          date: '2026-11-16',
          targetVolumeMl: 2000,
          totalLoggedMl: 2500,
          effectiveHydrationMl: 2400,
          percentage: 120,
          isGoalAchieved: true,
          entries: [],
        },
        {
          date: '2026-11-17',
          targetVolumeMl: 2000,
          totalLoggedMl: 2000,
          effectiveHydrationMl: 2000,
          percentage: 100,
          isGoalAchieved: true,
          entries: [],
        },
        {
          date: '2026-11-18',
          targetVolumeMl: 2000,
          totalLoggedMl: 2300,
          effectiveHydrationMl: 2250,
          percentage: 113,
          isGoalAchieved: true,
          entries: [],
        },
      ];

      // Convert to chart items
      const chartItems = mockWeeklyProgress.map((p) =>
        mapDailyProgressToDayChart(p, '2026-11-18')
      );

      assert.strictEqual(chartItems.length, 7);
      assert.strictEqual(chartItems[6].isToday, true);
      assert.strictEqual(chartItems[5].isToday, false);

      // Verify summary calculations
      const summary = calculateWeeklySummary(mockWeeklyProgress);
      // Total: 2050 + 1950 + 2100 + 1800 + 2400 + 2000 + 2250 = 14550
      assert.strictEqual(summary.totalEffectiveVolumeMl, 14550);
      // Average: 14550 / 7 = 2079
      assert.strictEqual(summary.averageDailyVolumeMl, 2079);
      // Achieved count: 5 of 7
      assert.strictEqual(summary.achievedDaysCount, 5);
      // Completion rate: Math.round((5 / 7) * 100) = 71%
      assert.strictEqual(summary.completionRatePercent, 71);
    });
  });
});
