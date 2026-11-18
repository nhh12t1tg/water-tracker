import { AdMobConfig } from '../types/water';

declare const process: {
  env: Record<string, string | undefined>;
};

/**
 * AdMob Development & Production Configuration
 * 
 * Official Google Android Test Ad Unit IDs:
 * Reference: https://developers.google.com/admob/android/test-ads
 * 
 * CRITICAL SAFETY RULE:
 * Always use test ad unit IDs during development and automated QA testing.
 * Clicking your own live ads or serving live ads during testing causes immediate account suspension.
 */
export const DEV_ADMOB_CONFIG: AdMobConfig = {
  // Google Official Sample App ID for Android
  appId: 'ca-app-pub-3940256099942544~3347511713',

  // Google Official Test Banner ID (Adaptive Banner)
  bannerAdUnitId: 'ca-app-pub-3940256099942544/6300978111',

  // Google Official Test Interstitial ID
  interstitialAdUnitId: 'ca-app-pub-3940256099942544/1033173712',

  // Google Official Test Rewarded Video ID
  rewardedAdUnitId: 'ca-app-pub-3940256099942544/5224354917',

  isTesting: true,

  // Add physical test device hashed IDs here to test real devices safely
  testDeviceIds: [
    'EMULATOR', // Android Studio Emulator
  ],

  // 3 minutes cooldown (180,000 ms) strictly enforced to avoid Disruptive Ads strikes
  interstitialCooldownMs: 180000,
};

export const PROD_ADMOB_CONFIG: AdMobConfig = {
  // Production IDs will be populated through build variants / Gradle BuildConfig
  appId: (typeof process !== 'undefined' && process.env?.ADMOB_APP_ID) || '',
  bannerAdUnitId: (typeof process !== 'undefined' && process.env?.ADMOB_BANNER_ID) || '',
  interstitialAdUnitId: (typeof process !== 'undefined' && process.env?.ADMOB_INTERSTITIAL_ID) || '',
  rewardedAdUnitId: (typeof process !== 'undefined' && process.env?.ADMOB_REWARDED_ID) || '',
  isTesting: false,
  testDeviceIds: [],
  interstitialCooldownMs: 180000,
};

export function getActiveAdMobConfig(): AdMobConfig {
  const isDev = typeof process === 'undefined' || process.env?.NODE_ENV !== 'production';
  return isDev ? DEV_ADMOB_CONFIG : PROD_ADMOB_CONFIG;
}
