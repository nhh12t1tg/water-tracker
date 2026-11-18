import type {
  DailyProgress,
  Gender,
  ActivityLevel,
  WaterLogEntry,
  StreakState,
} from '../../../types/water.ts';

export interface DayChartData {
  dayName: string;
  date: string;
  intakeMl: number;
  targetMl: number;
  percentage: number;
  isGoalAchieved: boolean;
  isToday: boolean;
}

export interface StreakMilestone {
  title: string;
  targetDays: number;
  description: string;
  isUnlocked: boolean;
  icon: string;
}

export interface WeeklySummary {
  totalEffectiveVolumeMl: number;
  averageDailyVolumeMl: number;
  achievedDaysCount: number;
  completionRatePercent: number;
}

/**
 * Standard beverage hydration factors per PRD specification
 */
export const BEVERAGE_FACTORS: Record<string, number> = {
  water: 1.0,
  tea: 0.8,
  coffee: 0.8,
  milk: 0.9,
  juice: 0.9,
  electrolytes: 1.0,
  soda: 0.7,
  alcohol: 0.0,
};

/**
 * Activity level offsets in milliliters
 */
export const ACTIVITY_OFFSETS: Record<ActivityLevel, number> = {
  sedentary: 0,
  moderate: 350,
  active: 700,
};

/**
 * Sanitize weight to prevent negative, zero, or absurd inputs.
 * Floor at 50kg, cap at 150kg for formula sanity (Review Focus 1).
 */
export function sanitizeWeightKg(weightKg: number): number {
  if (!Number.isFinite(weightKg) || weightKg <= 0) {
    return 50;
  }
  if (weightKg > 300) {
    return 150;
  }
  if (weightKg < 30) {
    return 50;
  }
  return weightKg;
}

/**
 * Calculate recommended daily water intake goal based on profile.
 * Formula: (weightKg * (gender === 'male' ? 35 : 31)) + activityOffset
 */
export function calculateDailyTarget(
  weightKg: number,
  gender: Gender,
  activity: ActivityLevel,
  customOverride?: number | null
): number {
  if (customOverride !== undefined && customOverride !== null && customOverride > 0) {
    return Math.round(customOverride);
  }

  const cleanWeight = sanitizeWeightKg(weightKg);
  const factor = gender === 'male' ? 35 : 31;
  const offset = ACTIVITY_OFFSETS[activity] ?? 0;
  const calculated = Math.round(cleanWeight * factor + offset);

  // Guarantee minimum positive threshold (at least 1000ml)
  return Math.max(1000, calculated);
}

/**
 * Calculate effective hydration volume credited towards goal.
 * Multiplies raw volume by beverage efficiency factor and rounds to integer
 * strictly avoiding floating-point precision drift (Review Focus 5).
 */
export function calculateEffectiveVolume(rawVolumeMl: number, hydrationFactor: number): number {
  if (rawVolumeMl <= 0 || hydrationFactor <= 0) {
    return 0;
  }
  const cleanFactor = Math.max(0, Math.min(1.0, hydrationFactor));
  return Math.round(rawVolumeMl * cleanFactor);
}

/**
 * Calculate daily progress summary from an array of log entries
 */
export function calculateDailyProgress(
  date: string,
  targetVolumeMl: number,
  entries: WaterLogEntry[]
): DailyProgress {
  const safeTarget = Math.max(1, targetVolumeMl);
  const totalLoggedMl = entries.reduce((acc, curr) => acc + curr.rawVolumeMl, 0);
  const effectiveHydrationMl = entries.reduce((acc, curr) => acc + curr.effectiveVolumeMl, 0);
  const percentage = Math.round((effectiveHydrationMl / safeTarget) * 100);
  const isGoalAchieved = effectiveHydrationMl >= safeTarget;

  return {
    date,
    targetVolumeMl: safeTarget,
    totalLoggedMl,
    effectiveHydrationMl,
    percentage,
    isGoalAchieved,
    entries,
  };
}

/**
 * Streak milestone definitions and unlock status evaluator
 */
export function getStreakMilestones(currentStreak: number): StreakMilestone[] {
  return [
    {
      title: 'Khởi Đầu',
      targetDays: 3,
      description: 'Uống đủ nước 3 ngày liên tiếp',
      isUnlocked: currentStreak >= 3,
      icon: 'fire-starter',
    },
    {
      title: 'Kiên Trì 1 Tuần',
      targetDays: 7,
      description: 'Duy trì thói quen uống nước 7 ngày',
      isUnlocked: currentStreak >= 7,
      icon: 'fire-week',
    },
    {
      title: 'Thói Quen Vàng',
      targetDays: 14,
      description: '2 tuần cấp ẩm liên tục cho cơ thể',
      isUnlocked: currentStreak >= 14,
      icon: 'fire-gold',
    },
    {
      title: 'Bậc Thầy Nước',
      targetDays: 30,
      description: 'Thói quen vàng 30 ngày kỷ luật',
      isUnlocked: currentStreak >= 30,
      icon: 'fire-master',
    },
  ];
}

const VIETNAMESE_DAY_NAMES = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];

/**
 * Format date string (YYYY-MM-DD) to DayChartData item
 */
export function mapDailyProgressToDayChart(
  daily: DailyProgress,
  todayDateStr?: string
): DayChartData {
  const dateObj = new Date(daily.date + 'T12:00:00Z');
  const dayIndex = dateObj.getUTCDay();
  const dayName = VIETNAMESE_DAY_NAMES[dayIndex] || 'T?';
  const isToday = todayDateStr ? daily.date === todayDateStr : false;

  return {
    dayName,
    date: daily.date,
    intakeMl: daily.effectiveHydrationMl,
    targetMl: daily.targetVolumeMl,
    percentage: daily.percentage,
    isGoalAchieved: daily.isGoalAchieved,
    isToday,
  };
}

/**
 * Compute overall weekly summary statistics
 */
export function calculateWeeklySummary(weeklyLogs: DailyProgress[]): WeeklySummary {
  if (!weeklyLogs || weeklyLogs.length === 0) {
    return {
      totalEffectiveVolumeMl: 0,
      averageDailyVolumeMl: 0,
      achievedDaysCount: 0,
      completionRatePercent: 0,
    };
  }

  const totalEffectiveVolumeMl = weeklyLogs.reduce(
    (acc, curr) => acc + curr.effectiveHydrationMl,
    0
  );
  const averageDailyVolumeMl = Math.round(totalEffectiveVolumeMl / weeklyLogs.length);
  const achievedDaysCount = weeklyLogs.filter((d) => d.isGoalAchieved).length;
  const completionRatePercent = Math.round((achievedDaysCount / weeklyLogs.length) * 100);

  return {
    totalEffectiveVolumeMl,
    averageDailyVolumeMl,
    achievedDaysCount,
    completionRatePercent,
  };
}
