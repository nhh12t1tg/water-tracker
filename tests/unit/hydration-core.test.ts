import { describe, it } from 'node:test';
import assert from 'node:assert';
import {
  calculateDailyTarget,
  calculateEffectiveVolume,
  calculateDailyProgress,
  sanitizeWeightKg,
  BEVERAGE_FACTORS,
} from '../../features/history/utils/hydration-math.ts';
import type { WaterLogEntry, BeverageId, LogEntryId } from '../../types/water.ts';

describe('Hydration Core Unit Tests', () => {
  describe('Daily Water Intake Target Calculation', () => {
    it('calculates target for male with different activity levels', () => {
      // Base: 70kg * 35 = 2450
      assert.strictEqual(calculateDailyTarget(70, 'male', 'sedentary'), 2450);
      // Moderate: +350 = 2800
      assert.strictEqual(calculateDailyTarget(70, 'male', 'moderate'), 2800);
      // Active: +700 = 3150
      assert.strictEqual(calculateDailyTarget(70, 'male', 'active'), 3150);
    });

    it('calculates target for female with different activity levels', () => {
      // Base: 60kg * 31 = 1860
      assert.strictEqual(calculateDailyTarget(60, 'female', 'sedentary'), 1860);
      // Moderate: +350 = 2210
      assert.strictEqual(calculateDailyTarget(60, 'female', 'moderate'), 2210);
      // Active: +700 = 2560
      assert.strictEqual(calculateDailyTarget(60, 'female', 'active'), 2560);
    });

    it('calculates target for gender "other" using base factor 31', () => {
      assert.strictEqual(calculateDailyTarget(55, 'other', 'sedentary'), 1705);
      assert.strictEqual(calculateDailyTarget(55, 'other', 'moderate'), 2055);
    });

    it('honors manual goal override when provided', () => {
      const manualTarget = 3200;
      assert.strictEqual(calculateDailyTarget(70, 'male', 'sedentary', manualTarget), 3200);
      assert.strictEqual(calculateDailyTarget(60, 'female', 'active', manualTarget), 3200);
    });

    it('ignores null or undefined custom override and uses formula', () => {
      assert.strictEqual(calculateDailyTarget(70, 'male', 'sedentary', null), 2450);
      assert.strictEqual(calculateDailyTarget(70, 'male', 'sedentary', undefined), 2450);
    });
  });

  describe('Review Focus 1: Boundary Weight Calculation & Negative Goal Prevention', () => {
    it('sanitizes weight <= 0 to safe default of 50kg', () => {
      assert.strictEqual(sanitizeWeightKg(0), 50);
      assert.strictEqual(sanitizeWeightKg(-15), 50);
      assert.strictEqual(sanitizeWeightKg(-100), 50);

      // Calculates safe positive target even with negative or zero input
      const targetZero = calculateDailyTarget(0, 'male', 'sedentary');
      assert.strictEqual(targetZero, 1750); // 50 * 35 + 0
      assert.ok(targetZero > 0, 'Target must never be zero or negative');

      const targetNegative = calculateDailyTarget(-50, 'female', 'sedentary');
      assert.strictEqual(targetNegative, 1550); // 50 * 31 + 0
      assert.ok(targetNegative > 0, 'Target must never be negative');
    });

    it('sanitizes weight > 300 to safe maximum of 150kg', () => {
      assert.strictEqual(sanitizeWeightKg(350), 150);
      assert.strictEqual(sanitizeWeightKg(999), 150);

      const targetCapped = calculateDailyTarget(500, 'male', 'active');
      assert.strictEqual(targetCapped, 150 * 35 + 700); // 5950
    });

    it('sanitizes invalid or non-finite inputs gracefully', () => {
      assert.strictEqual(sanitizeWeightKg(NaN), 50);
      assert.strictEqual(sanitizeWeightKg(Infinity), 50);
      assert.strictEqual(sanitizeWeightKg(-Infinity), 50);
    });

    it('enforces a minimum threshold of at least 1000ml', () => {
      const minTarget = calculateDailyTarget(30, 'female', 'sedentary');
      assert.ok(minTarget >= 1000, `Minimum target should be at least 1000ml, got ${minTarget}`);
    });
  });

  describe('Review Focus 5: Effective Volume Floating-Point Accuracy', () => {
    it('calculates coffee/tea (factor 0.8) without floating-point precision drift', () => {
      // 250 * 0.8 in IEEE 754 often drifts to 199.99999999999997
      const effectiveCoffee = calculateEffectiveVolume(250, BEVERAGE_FACTORS.coffee);
      assert.strictEqual(typeof effectiveCoffee, 'number');
      assert.strictEqual(Number.isInteger(effectiveCoffee), true);
      assert.strictEqual(effectiveCoffee, 200, '250ml coffee must equal integer 200ml');

      const effectiveTea = calculateEffectiveVolume(330, BEVERAGE_FACTORS.tea);
      assert.strictEqual(Number.isInteger(effectiveTea), true);
      assert.strictEqual(effectiveTea, 264);

      const smallCoffee = calculateEffectiveVolume(150, 0.8);
      assert.strictEqual(smallCoffee, 120);
    });

    it('calculates pure water (factor 1.0) with exact 1:1 match', () => {
      assert.strictEqual(calculateEffectiveVolume(250, BEVERAGE_FACTORS.water), 250);
      assert.strictEqual(calculateEffectiveVolume(500, BEVERAGE_FACTORS.water), 500);
      assert.strictEqual(calculateEffectiveVolume(1000, BEVERAGE_FACTORS.water), 1000);
    });

    it('calculates milk/juice (factor 0.9) accurately', () => {
      assert.strictEqual(calculateEffectiveVolume(250, BEVERAGE_FACTORS.juice), 225);
      assert.strictEqual(calculateEffectiveVolume(300, BEVERAGE_FACTORS.milk), 270);
    });

    it('calculates electrolytes/sports drinks (factor 1.0) accurately', () => {
      assert.strictEqual(calculateEffectiveVolume(500, BEVERAGE_FACTORS.electrolytes), 500);
    });

    it('handles zero or negative volume gracefully', () => {
      assert.strictEqual(calculateEffectiveVolume(0, 1.0), 0);
      assert.strictEqual(calculateEffectiveVolume(-250, 1.0), 0);
      assert.strictEqual(calculateEffectiveVolume(250, -0.5), 0);
    });
  });

  describe('Daily Progress Aggregation', () => {
    it('aggregates multiple beverage logs into daily totals', () => {
      const mockEntries: WaterLogEntry[] = [
        {
          id: 'log-1' as LogEntryId,
          timestamp: '2026-11-18T08:00:00Z',
          beverageId: 'water' as BeverageId,
          rawVolumeMl: 250,
          effectiveVolumeMl: 250,
        },
        {
          id: 'log-2' as LogEntryId,
          timestamp: '2026-11-18T10:30:00Z',
          beverageId: 'coffee' as BeverageId,
          rawVolumeMl: 250,
          effectiveVolumeMl: 200, // 0.8 multiplier
        },
        {
          id: 'log-3' as LogEntryId,
          timestamp: '2026-11-18T14:15:00Z',
          beverageId: 'juice' as BeverageId,
          rawVolumeMl: 200,
          effectiveVolumeMl: 180, // 0.9 multiplier
        },
      ];

      const targetMl = 2000;
      const progress = calculateDailyProgress('2026-11-18', targetMl, mockEntries);

      assert.strictEqual(progress.date, '2026-11-18');
      assert.strictEqual(progress.targetVolumeMl, 2000);
      assert.strictEqual(progress.totalLoggedMl, 700); // 250 + 250 + 200
      assert.strictEqual(progress.effectiveHydrationMl, 630); // 250 + 200 + 180
      assert.strictEqual(progress.percentage, 32); // Math.round((630 / 2000) * 100)
      assert.strictEqual(progress.isGoalAchieved, false);
      assert.strictEqual(progress.entries.length, 3);
    });

    it('marks isGoalAchieved true when effective hydration reaches or exceeds target', () => {
      const mockEntries: WaterLogEntry[] = [
        {
          id: 'log-1' as LogEntryId,
          timestamp: '2026-11-18T09:00:00Z',
          beverageId: 'water' as BeverageId,
          rawVolumeMl: 1000,
          effectiveVolumeMl: 1000,
        },
        {
          id: 'log-2' as LogEntryId,
          timestamp: '2026-11-18T15:00:00Z',
          beverageId: 'water' as BeverageId,
          rawVolumeMl: 1000,
          effectiveVolumeMl: 1000,
        },
      ];

      const progress = calculateDailyProgress('2026-11-18', 2000, mockEntries);
      assert.strictEqual(progress.effectiveHydrationMl, 2000);
      assert.strictEqual(progress.percentage, 100);
      assert.strictEqual(progress.isGoalAchieved, true);
    });

    it('handles empty entries without crashing or division by zero', () => {
      const progress = calculateDailyProgress('2026-11-18', 2000, []);
      assert.strictEqual(progress.totalLoggedMl, 0);
      assert.strictEqual(progress.effectiveHydrationMl, 0);
      assert.strictEqual(progress.percentage, 0);
      assert.strictEqual(progress.isGoalAchieved, false);
    });
  });
});
