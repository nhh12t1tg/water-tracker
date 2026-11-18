import {
  UserProfile,
  WaterLogEntry,
  DailyProgress,
  UserId,
  BeverageId,
  LogEntryId,
  Gender,
  ActivityLevel,
} from '../../types/water';
import { calculateIntakeGoal } from '../../features/onboarding/utils/calculator';
import { ISqliteDriver, SqliteDriver } from './sqlite-driver';

export const DEFAULT_DAILY_TARGET_ML = 2000;

interface UserProfileRow {
  id: string;
  gender: string;
  weight_kg: number;
  activity_level: string;
  custom_daily_goal_ml: number | null;
  wake_up_time: string;
  bed_time: string;
  notification_interval_minutes: number;
  is_dnd_enabled: number;
  created_at: string;
  updated_at: string;
}

interface WaterLogRow {
  id: string;
  date_str: string;
  timestamp: string;
  beverage_id: string;
  raw_volume_ml: number;
  effective_volume_ml: number;
}

export function extractDateStr(timestamp: string): string {
  if (!timestamp) {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
  const parts = timestamp.split('T');
  if (parts[0] && /^\d{4}-\d{2}-\d{2}$/.test(parts[0])) {
    return parts[0];
  }
  const parsed = new Date(timestamp);
  if (!Number.isNaN(parsed.getTime())) {
    return `${parsed.getFullYear()}-${String(parsed.getMonth() + 1).padStart(2, '0')}-${String(parsed.getDate()).padStart(2, '0')}`;
  }
  return new Date().toISOString().split('T')[0];
}

export function getPast7Dates(endDateStr: string): string[] {
  const dates: string[] = [];
  const [year, month, day] = endDateStr.split('-').map(Number);
  const baseDate = new Date(Date.UTC(year, month - 1, day));

  for (let i = 6; i >= 0; i--) {
    const d = new Date(baseDate);
    d.setUTCDate(baseDate.getUTCDate() - i);
    const dateStr = d.toISOString().split('T')[0];
    dates.push(dateStr);
  }
  return dates;
}

/**
 * WaterStorageService - Local SQLite/Room persistence store for Water Tracker.
 */
export class WaterStorageService {
  private driver: ISqliteDriver;
  private isInitialized: boolean = false;

  constructor(driver?: ISqliteDriver) {
    this.driver = driver || new SqliteDriver(':memory:');
  }

  public async initialize(): Promise<void> {
    if (this.isInitialized) return;

    await this.driver.initialize();

    // Initialize UserProfile Table
    await this.driver.execute(`
      CREATE TABLE IF NOT EXISTS user_profile (
        id TEXT PRIMARY KEY,
        gender TEXT NOT NULL,
        weight_kg REAL NOT NULL,
        activity_level TEXT NOT NULL,
        custom_daily_goal_ml INTEGER,
        wake_up_time TEXT NOT NULL,
        bed_time TEXT NOT NULL,
        notification_interval_minutes INTEGER NOT NULL,
        is_dnd_enabled INTEGER NOT NULL,
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL
      )
    `);

    // Initialize Water Logs Table
    await this.driver.execute(`
      CREATE TABLE IF NOT EXISTS water_logs (
        id TEXT PRIMARY KEY,
        date_str TEXT NOT NULL,
        timestamp TEXT NOT NULL,
        beverage_id TEXT NOT NULL,
        raw_volume_ml INTEGER NOT NULL,
        effective_volume_ml INTEGER NOT NULL
      )
    `);

    this.isInitialized = true;
  }

  public async saveUserProfile(profile: UserProfile): Promise<void> {
    if (!this.isInitialized) await this.initialize();

    await this.driver.execute(
      `INSERT OR REPLACE INTO user_profile (
        id, gender, weight_kg, activity_level, custom_daily_goal_ml,
        wake_up_time, bed_time, notification_interval_minutes, is_dnd_enabled,
        created_at, updated_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        profile.id,
        profile.gender,
        profile.weightKg,
        profile.activityLevel,
        profile.customDailyGoalMl,
        profile.wakeUpTime,
        profile.bedTime,
        profile.notificationIntervalMinutes,
        profile.isDndEnabled ? 1 : 0,
        profile.createdAt,
        profile.updatedAt,
      ]
    );
  }

  public async getUserProfile(): Promise<UserProfile | null> {
    if (!this.isInitialized) await this.initialize();

    const rows = await this.driver.query<UserProfileRow>(
      `SELECT * FROM user_profile LIMIT 1`
    );

    if (rows.length === 0) return null;

    const row = rows[0];
    return {
      id: row.id as UserId,
      gender: row.gender as Gender,
      weightKg: row.weight_kg,
      activityLevel: row.activity_level as ActivityLevel,
      customDailyGoalMl: row.custom_daily_goal_ml !== null ? Number(row.custom_daily_goal_ml) : null,
      wakeUpTime: row.wake_up_time,
      bedTime: row.bed_time,
      notificationIntervalMinutes: Number(row.notification_interval_minutes),
      isDndEnabled: Boolean(row.is_dnd_enabled),
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }

  public async logDrink(entry: WaterLogEntry): Promise<DailyProgress> {
    if (!this.isInitialized) await this.initialize();

    const dateStr = extractDateStr(entry.timestamp);
    const sanitizedEffective = Math.round(entry.effectiveVolumeMl);
    const sanitizedRaw = Math.round(entry.rawVolumeMl);

    await this.driver.execute(
      `INSERT OR REPLACE INTO water_logs (
        id, date_str, timestamp, beverage_id, raw_volume_ml, effective_volume_ml
      ) VALUES (?, ?, ?, ?, ?, ?)`,
      [
        entry.id,
        dateStr,
        entry.timestamp,
        entry.beverageId,
        sanitizedRaw,
        sanitizedEffective,
      ]
    );

    return this.getDailyProgress(dateStr);
  }

  public async getDailyProgress(dateStr: string): Promise<DailyProgress> {
    if (!this.isInitialized) await this.initialize();

    // Determine target volume from UserProfile
    const profile = await this.getUserProfile();
    let targetVolumeMl = DEFAULT_DAILY_TARGET_ML;

    if (profile) {
      if (profile.customDailyGoalMl !== null && profile.customDailyGoalMl > 0) {
        targetVolumeMl = profile.customDailyGoalMl;
      } else {
        targetVolumeMl = calculateIntakeGoal(
          profile.weightKg,
          profile.gender,
          profile.activityLevel
        );
      }
    }

    const rows = await this.driver.query<WaterLogRow>(
      `SELECT * FROM water_logs WHERE date_str = ? ORDER BY timestamp ASC`,
      [dateStr]
    );

    const entries: WaterLogEntry[] = rows.map((r) => ({
      id: r.id as LogEntryId,
      timestamp: r.timestamp,
      beverageId: r.beverage_id as BeverageId,
      rawVolumeMl: r.raw_volume_ml,
      effectiveVolumeMl: r.effective_volume_ml,
    }));

    let totalLoggedMl = 0;
    let effectiveHydrationMl = 0;

    for (const e of entries) {
      totalLoggedMl += e.rawVolumeMl;
      effectiveHydrationMl += e.effectiveVolumeMl;
    }
    effectiveHydrationMl = Math.round(effectiveHydrationMl);

    const percentage = targetVolumeMl > 0
      ? Math.round((effectiveHydrationMl * 100) / targetVolumeMl)
      : 0;

    const isGoalAchieved = targetVolumeMl > 0 && effectiveHydrationMl >= targetVolumeMl;

    return {
      date: dateStr,
      targetVolumeMl,
      totalLoggedMl,
      effectiveHydrationMl,
      percentage,
      isGoalAchieved,
      entries,
    };
  }

  public async getWeeklyProgress(endDateStr: string): Promise<DailyProgress[]> {
    if (!this.isInitialized) await this.initialize();

    const dates = getPast7Dates(endDateStr);
    const results: DailyProgress[] = [];

    for (const d of dates) {
      const progress = await this.getDailyProgress(d);
      results.push(progress);
    }

    return results;
  }
}
