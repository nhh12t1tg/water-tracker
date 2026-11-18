import test from 'node:test';
import assert from 'node:assert/strict';
import { calculateIntakeGoal } from '../utils/calculator';
import { Gender, ActivityLevel } from '../../../types/water';

test('calculateIntakeGoal calculates baseline for male sedentary correctly', () => {
  // 70kg * 35 + 0 = 2450
  const goal = calculateIntakeGoal(70, 'male', 'sedentary');
  assert.strictEqual(goal, 2450);
});

test('calculateIntakeGoal calculates baseline for female moderate correctly', () => {
  // 60kg * 31 + 350 = 1860 + 350 = 2210
  const goal = calculateIntakeGoal(60, 'female', 'moderate');
  assert.strictEqual(goal, 2210);
});

test('calculateIntakeGoal calculates other gender with active offset correctly', () => {
  // 80kg * 31 + 700 = 2480 + 700 = 3180
  const goal = calculateIntakeGoal(80, 'other', 'active');
  assert.strictEqual(goal, 3180);
});

test('calculateIntakeGoal clamps weight lower bound to 30kg', () => {
  // 20kg clamped to 30kg: 30 * 35 = 1050
  const goal = calculateIntakeGoal(20, 'male', 'sedentary');
  assert.strictEqual(goal, 1050);

  // Negative weight clamped to 30kg
  const goalNeg = calculateIntakeGoal(-10, 'male', 'sedentary');
  assert.strictEqual(goalNeg, 1050);
});

test('calculateIntakeGoal clamps weight upper bound to 250kg', () => {
  // 300kg clamped to 250kg: 250 * 35 + 700 = 8750 + 700 = 9450
  const goal = calculateIntakeGoal(300, 'male', 'active');
  assert.strictEqual(goal, 9450);
});

test('calculateIntakeGoal handles invalid/NaN weight with default 70kg', () => {
  const goal = calculateIntakeGoal(NaN, 'male', 'sedentary');
  assert.strictEqual(goal, 2450);
});

test('calculateIntakeGoal respects customOverride when provided', () => {
  const goal = calculateIntakeGoal(70, 'male', 'sedentary', 3200);
  assert.strictEqual(goal, 3200);

  // If customOverride is null or undefined, fallback to formula
  const goalNull = calculateIntakeGoal(70, 'male', 'sedentary', null);
  assert.strictEqual(goalNull, 2450);
});
