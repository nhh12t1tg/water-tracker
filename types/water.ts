/**
 * WATER TRACKER APP - CORE CONTRACT & DOMAIN TYPES
 * File: types/water.ts
 * 
 * IMPORTANT: This file is a FROZEN CONTRACT (READ-ONLY).
 * All agents working in parallel MUST adhere to these interfaces.
 * Do not modify without cross-agent architectural alignment.
 */

// ==========================================
// 1. BRANDED IDENTIFIERS
// ==========================================
export type BeverageId = string & { readonly __brand: 'BeverageId' };
export type LogEntryId = string & { readonly __brand: 'LogEntryId' };
export type UserId = string & { readonly __brand: 'UserId' };

// ==========================================
// 2. USER PROFILE & TARGET CALCULATION
// ==========================================
export type Gender = 'male' | 'female' | 'other';

export type ActivityLevel = 'sedentary' | 'moderate' | 'active';

export interface UserProfile {
  id: UserId;
  gender: Gender;
  weightKg: number;
  activityLevel: ActivityLevel;
  /**
   * User override for daily goal in milliliters.
   * If null, goal is calculated via formula:
   * (weightKg * (gender === 'male' ? 35 : 31)) + activityOffset
   */
  customDailyGoalMl: number | null;
  /** Wake-up time format 'HH:mm', e.g., '07:00' */
  wakeUpTime: string;
  /** Bedtime format 'HH:mm', e.g., '22:00' */
  bedTime: string;
  /** Interval in minutes between smart reminders (e.g., 90 or 120 mins) */
  notificationIntervalMinutes: number;
  /** Strict Do Not Disturb during bedTime -> wakeUpTime */
  isDndEnabled: boolean;
  createdAt: string; // ISO 8601
  updatedAt: string; // ISO 8601
}

// ==========================================
// 3. BEVERAGES & HYDRATION FACTOR
// ==========================================
export interface BeverageType {
  id: BeverageId;
  name: string;
  /**
   * Hydration efficiency multiplier (0.0 to 1.0)
   * e.g., Water = 1.0, Tea/Coffee = 0.8, Milk/Juice = 0.9, Electrolytes = 1.0
   */
  hydrationFactor: number;
  iconName: string;
  defaultServingSizeMl: number;
  isUnlocked: boolean;
  isCustom: boolean;
}

// ==========================================
// 4. WATER LOG ENTRY & DAILY PROGRESS
// ==========================================
export interface WaterLogEntry {
  id: LogEntryId;
  timestamp: string; // ISO 8601
  beverageId: BeverageId;
  /** Actual volume consumed in ml (e.g. 250ml) */
  rawVolumeMl: number;
  /** Effective volume credited towards daily target (rawVolumeMl * hydrationFactor) */
  effectiveVolumeMl: number;
}

export interface DailyProgress {
  /** Date format 'YYYY-MM-DD' */
  date: string;
  /** Target volume in ml for the day */
  targetVolumeMl: number;
  /** Total raw volume consumed across all entries */
  totalLoggedMl: number;
  /** Sum of effectiveVolumeMl credited towards hydration target */
  effectiveHydrationMl: number;
  /** Progress percentage (0 - 100+%) */
  percentage: number;
  /** True if effectiveHydrationMl >= targetVolumeMl */
  isGoalAchieved: boolean;
  /** List of individual logs for this date */
  entries: WaterLogEntry[];
}

// ==========================================
// 5. STREAK STATE & RETENTION
// ==========================================
export interface StreakState {
  currentStreakDays: number;
  longestStreakDays: number;
  /** Last date the goal was achieved 'YYYY-MM-DD' */
  lastAchievedDate: string | null;
  /** Number of streak freeze tokens available (earned or granted via Rewarded Ads) */
  availableFreezes: number;
  /** Whether today or yesterday was protected by a freeze token */
  isFrozenForDate: boolean;
}

// ==========================================
// 6. ADMOB CONFIGURATION & POLICY COMPLIANCE
// ==========================================
export type RewardRewardType = 'UNLOCK_THEME' | 'FREEZE_STREAK';

export interface AdMobConfig {
  appId: string;
  bannerAdUnitId: string;
  interstitialAdUnitId: string;
  rewardedAdUnitId: string;
  isTesting: boolean;
  testDeviceIds: string[];
  /**
   * Minimum cooldown interval between two interstitial ad impressions in milliseconds.
   * Enforces Google Better Ads policy against disruptive ads (Default: 240,000ms = 4 mins).
   */
  interstitialCooldownMs: number;
}

export type AdEventCallback = () => void;
export type AdErrorCallback = (errorMessage: string) => void;

/**
 * Facade Interface for AdMob interactions.
 * UI components must NEVER interact directly with Google Mobile Ads SDK classes.
 * All access must be mediated through this interface.
 */
export interface IAdMobService {
  /** Initialize Google Mobile Ads SDK */
  initialize(): Promise<void>;

  /** Whether the AdMob SDK has been initialized */
  isInitialized(): boolean;

  /** Preload interstitial ads to be ready for natural transition points */
  preloadInterstitial(): void;

  /** Preload rewarded video ads */
  preloadRewarded(): void;

  /** Check if an interstitial can be shown according to frequency capping rules */
  canShowInterstitial(): boolean;

  /**
   * Display an Interstitial Ad at a natural break/transition point.
   * Returns false if blocked by cooldown or ad is not loaded.
   */
  showInterstitial(
    onDismissed?: AdEventCallback,
    onError?: AdErrorCallback
  ): boolean;

  /**
   * Display a Rewarded Video Ad when user explicitly taps a reward trigger.
   * @param rewardType Intent of the reward (theme unlock vs streak freeze)
   * @param onRewardGranted Called when user completes video and earns reward
   * @param onDismissed Called when ad closes
   * @param onError Called if ad fails to show
   */
  showRewarded(
    rewardType: RewardRewardType,
    onRewardGranted: (rewardType: RewardRewardType) => void,
    onDismissed?: AdEventCallback,
    onError?: AdErrorCallback
  ): void;

  /** Record that an interstitial was just shown to reset cooldown timer */
  recordInterstitialImpression(): void;
}
