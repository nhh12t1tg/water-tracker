import { Gender, ActivityLevel } from '../../../types/water';

export const MIN_WEIGHT_KG = 30;
export const MAX_WEIGHT_KG = 250;
export const DEFAULT_WEIGHT_KG = 70;

export const ACTIVITY_OFFSETS: Record<ActivityLevel, number> = {
  sedentary: 0,
  moderate: 350,
  active: 700,
};

/**
 * Sanitizes weight input to ensure it falls within realistic human physiological bounds.
 * Prevents negative or astronomical values from breaking hydration goal calculations.
 */
export function clampWeight(weightKg: number): number {
  if (!Number.isFinite(weightKg) || Number.isNaN(weightKg)) {
    return DEFAULT_WEIGHT_KG;
  }
  return Math.max(MIN_WEIGHT_KG, Math.min(MAX_WEIGHT_KG, weightKg));
}

/**
 * Calculates recommended daily water intake in milliliters.
 *
 * Formula:
 *  (weightKg * (gender === 'male' ? 35 : 31)) + activityOffset
 *  - Sedentary: +0ml
 *  - Moderate: +350ml
 *  - Active: +700ml
 *
 * If `customOverride` is provided (positive number), it takes precedence over the formula.
 */
export function calculateIntakeGoal(
  weightKg: number,
  gender: Gender,
  activity: ActivityLevel,
  customOverride?: number | null
): number {
  if (customOverride !== undefined && customOverride !== null && customOverride > 0) {
    return Math.round(customOverride);
  }

  const sanitizedWeight = clampWeight(weightKg);
  const multiplier = gender === 'male' ? 35 : 31;
  const activityOffset = ACTIVITY_OFFSETS[activity] ?? 0;

  return Math.round(sanitizedWeight * multiplier + activityOffset);
}
