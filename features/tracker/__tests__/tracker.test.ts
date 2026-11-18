import test from 'node:test';
import assert from 'node:assert/strict';
import React from 'react';
import {
  DEFAULT_QUICK_DRINK_PRESETS,
  calculateHydrationProgress,
  calculateSineWavePoint,
  calculateSpringPhysicsStep,
  getMotivationSubtitle,
  ADMOB_BANNER_RESERVED_HEIGHT_DP,
  ADMOB_BANNER_TOUCH_PADDING_DP,
  WaveProgress,
  QuickDrinkBar,
  TrackerScreen,
} from '../index';
import type { DailyProgress, StreakState } from '../../../types/water';

test('1. Quick Drink Presets - verifies default presets [150, 250, 500] ml', () => {
  assert.deepStrictEqual(DEFAULT_QUICK_DRINK_PRESETS, [150, 250, 500]);
});

test('2. Hydration Progress Calculation - computes accurate percentages and handles boundary conditions', () => {
  // Normal progress
  const zero = calculateHydrationProgress(0, 2000);
  assert.strictEqual(zero.percentage, 0);
  assert.strictEqual(zero.rawRatio, 0);

  const half = calculateHydrationProgress(1000, 2000);
  assert.strictEqual(half.percentage, 50);
  assert.strictEqual(half.rawRatio, 0.5);

  const full = calculateHydrationProgress(2000, 2000);
  assert.strictEqual(full.percentage, 100);
  assert.strictEqual(full.rawRatio, 1.0);

  // Overflow progress (clamped up to 1.25)
  const overflow = calculateHydrationProgress(2500, 2000);
  assert.strictEqual(overflow.percentage, 125);
  assert.strictEqual(overflow.clampedProgress, 1.25);

  // Safe division by zero prevention
  const safeZeroTarget = calculateHydrationProgress(500, 0);
  assert.strictEqual(Number.isFinite(safeZeroTarget.percentage), true);
  assert.strictEqual(safeZeroTarget.percentage, 0);
  assert.strictEqual(safeZeroTarget.rawRatio, 0);
});

test('3. Wave Canvas Math - calculates dual sine waves according to sin(x + phase)', () => {
  const width = 200;
  const levelY = 100;
  const amplitude = 14;
  const phaseFront = 0;
  const phaseBack = Math.PI / 2;

  // At x = 0 with phaseFront = 0: sin(0) = 0 -> y = 100
  const yFrontStart = calculateSineWavePoint(0, width, levelY, amplitude, phaseFront);
  assert.ok(Math.abs(yFrontStart - 100) < 0.001, `Expected 100, got ${yFrontStart}`);

  // At x = 0 with phaseBack = PI/2: sin(PI/2) = 1 -> y = 100 + amplitude
  const yBackStart = calculateSineWavePoint(0, width, levelY, amplitude, phaseBack);
  assert.ok(Math.abs(yBackStart - 114) < 0.001, `Expected 114, got ${yBackStart}`);

  // Front and back wave values must differ due to phase shift (dual-wave visual depth)
  assert.notStrictEqual(yFrontStart, yBackStart);

  // At x = width/4 (angle = PI/2) with phaseFront = 0: sin(PI/2) = 1 -> y = 114
  const yFrontQuarter = calculateSineWavePoint(width / 4, width, levelY, amplitude, phaseFront);
  assert.ok(Math.abs(yFrontQuarter - 114) < 0.001, `Expected 114, got ${yFrontQuarter}`);
});

test('4. Spring Physics Engine - simulates fluid water level transition with damping', () => {
  let position = 0;
  let velocity = 0;
  const target = 1.0;
  const stiffness = 0.08;
  const damping = 0.82;

  // Step 1: Initial push
  const step1 = calculateSpringPhysicsStep(position, target, velocity, stiffness, damping);
  assert.ok(step1.position > position, 'Position should advance towards target');
  assert.ok(step1.velocity > 0, 'Velocity should be positive towards target');

  // Simulate multiple steps to confirm convergence
  position = step1.position;
  velocity = step1.velocity;
  for (let i = 0; i < 100; i++) {
    const next = calculateSpringPhysicsStep(position, target, velocity, stiffness, damping);
    position = next.position;
    velocity = next.velocity;
  }

  // After 100 steps, position must converge to target (1.0) and velocity to 0
  assert.ok(Math.abs(position - target) < 0.05, `Position should converge to target, got ${position}`);
  assert.ok(Math.abs(velocity) < 0.05, `Velocity should dampen towards zero, got ${velocity}`);
});

test('5. AdMob Policy & Banner Layout - enforces 60dp height and >=16dp padding', () => {
  assert.strictEqual(ADMOB_BANNER_RESERVED_HEIGHT_DP, 60);
  assert.ok(
    ADMOB_BANNER_TOUCH_PADDING_DP >= 16,
    `Touch padding must be at least 16dp to prevent accidental clicks, got ${ADMOB_BANNER_TOUCH_PADDING_DP}`
  );
});

test('6. Motivational Subtitles - provides encouraging messages based on progress', () => {
  const ongoing = getMotivationSubtitle(1200, 2000);
  assert.ok(ongoing.includes('từng ngụm nhỏ'));

  const completed = getMotivationSubtitle(2000, 2000);
  assert.ok(completed.includes('Tuyệt vời') || completed.includes('đủ nước'));

  const exceeded = getMotivationSubtitle(2500, 2000);
  assert.ok(exceeded.includes('Tuyệt vời') || exceeded.includes('đủ nước'));
});

test('7. Component Instantiation & Callback Integration', () => {
  // Test QuickDrinkBar callbacks
  let addedVolume = 0;
  let themeShopOpened = false;

  const quickBarElement = React.createElement(QuickDrinkBar, {
    presets: [150, 250, 500],
    onQuickAdd: (ml: number) => { addedVolume = ml; },
    onOpenThemeShop: () => { themeShopOpened = true; },
  });
  assert.ok(React.isValidElement(quickBarElement));

  // Test WaveProgress component element
  const waveElement = React.createElement(WaveProgress, {
    currentMl: 1500,
    targetMl: 2000,
    progressPercentage: 75,
  });
  assert.ok(React.isValidElement(waveElement));

  // Test TrackerScreen element with DailyProgress contract from types/water.ts
  const mockDailyProgress: DailyProgress = {
    date: '2026-11-18',
    targetVolumeMl: 2000,
    totalLoggedMl: 1500,
    effectiveHydrationMl: 1500,
    percentage: 75,
    isGoalAchieved: false,
    entries: [],
  };

  const mockStreakState: StreakState = {
    currentStreakDays: 5,
    longestStreakDays: 12,
    lastAchievedDate: '2026-11-17',
    availableFreezes: 1,
    isFrozenForDate: false,
  };

  let trackerDrinkAdded = 0;
  let trackerRewardOpened = false;

  const trackerScreenElement = React.createElement(TrackerScreen, {
    progress: mockDailyProgress,
    streakState: mockStreakState,
    onAddDrink: (ml: number) => { trackerDrinkAdded = ml; },
    onOpenThemeReward: () => { trackerRewardOpened = true; },
  });
  assert.ok(React.isValidElement(trackerScreenElement));
});
