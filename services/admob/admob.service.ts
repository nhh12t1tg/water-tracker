import {
  IAdMobService,
  AdMobConfig,
  RewardRewardType,
  AdEventCallback,
  AdErrorCallback,
} from '../../types/water';
import { getActiveAdMobConfig, DEV_ADMOB_CONFIG } from '../../config/admob.config';

export interface PreloadedAd {
  readonly id: string;
  readonly adUnitId: string;
  readonly loadedAt: number;
}

/**
 * AdMobService - Singleton Facade implementation of IAdMobService.
 *
 * Architectural & Policy Compliance:
 * 1. Screens and ViewModels never directly instantiate Native Ad SDK objects.
 * 2. Interstitials are strictly frequency-capped by 3-minute (180,000ms) cooldown timer.
 * 3. Preload queues ensure zero latency during natural user break transitions.
 * 4. Rewarded ads guarantee callback integrity for Theme Unlocking and Streak Freezes.
 */
export class AdMobService implements IAdMobService {
  private static instance: AdMobService | null = null;
  private readonly config: AdMobConfig;
  private initialized: boolean = false;
  private lastInterstitialShownTimestamp: number = 0;

  // Preload queues for high reliability and zero screen freeze
  private interstitialQueue: PreloadedAd[] = [];
  private rewardedQueue: PreloadedAd[] = [];
  private readonly maxQueueCapacity: number = 2;

  private constructor(config?: AdMobConfig) {
    this.config = config || getActiveAdMobConfig();
  }

  /**
   * Access the shared Singleton instance
   */
  public static getInstance(config?: AdMobConfig): AdMobService {
    if (!AdMobService.instance) {
      AdMobService.instance = new AdMobService(config);
    }
    return AdMobService.instance;
  }

  /**
   * Reset instance for testing isolation
   */
  public static resetInstance(): void {
    AdMobService.instance = null;
  }

  public async initialize(): Promise<void> {
    if (this.initialized) return;

    console.log(`[AdMobService] Initializing Google Mobile Ads SDK with App ID: ${this.config.appId}`);
    if (this.config.isTesting) {
      console.log(`[AdMobService] Running in TEST MODE with test devices:`, this.config.testDeviceIds);
    }

    this.initialized = true;
    this.preloadInterstitial();
    this.preloadRewarded();
  }

  public isInitialized(): boolean {
    return this.initialized;
  }

  public preloadInterstitial(): void {
    if (!this.initialized) {
      console.warn('[AdMobService] Cannot preload interstitial: SDK not initialized.');
      return;
    }

    if (this.interstitialQueue.length >= this.maxQueueCapacity) {
      return;
    }

    console.log(`[AdMobService] Preloading Interstitial ad: ${this.config.interstitialAdUnitId}`);
    this.interstitialQueue.push({
      id: `interstitial_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
      adUnitId: this.config.interstitialAdUnitId,
      loadedAt: Date.now(),
    });
  }

  public preloadRewarded(): void {
    if (!this.initialized) {
      console.warn('[AdMobService] Cannot preload rewarded: SDK not initialized.');
      return;
    }

    if (this.rewardedQueue.length >= this.maxQueueCapacity) {
      return;
    }

    console.log(`[AdMobService] Preloading Rewarded ad: ${this.config.rewardedAdUnitId}`);
    this.rewardedQueue.push({
      id: `rewarded_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
      adUnitId: this.config.rewardedAdUnitId,
      loadedAt: Date.now(),
    });
  }

  public canShowInterstitial(): boolean {
    if (!this.initialized || this.interstitialQueue.length === 0) {
      return false;
    }

    const now = Date.now();
    const elapsedSinceLastAd = now - this.lastInterstitialShownTimestamp;
    const cooldownMet = elapsedSinceLastAd >= this.config.interstitialCooldownMs;

    if (!cooldownMet) {
      const remainingSeconds = Math.ceil((this.config.interstitialCooldownMs - elapsedSinceLastAd) / 1000);
      console.warn(`[AdMobService] Interstitial blocked by frequency cap. Cooldown active for ${remainingSeconds}s.`);
      return false;
    }

    return true;
  }

  public showInterstitial(
    onDismissed?: AdEventCallback,
    onError?: AdErrorCallback
  ): boolean {
    if (!this.canShowInterstitial()) {
      if (onError) {
        onError('Interstitial ad not ready or blocked by frequency cooldown.');
      }
      return false;
    }

    try {
      console.log(`[AdMobService] Presenting Interstitial Ad at natural transition.`);
      // Dequeue from preloaded ads
      this.interstitialQueue.shift();
      this.recordInterstitialImpression();

      if (onDismissed) {
        onDismissed();
      }

      // Immediately replenish preload queue for the next transition
      this.preloadInterstitial();
      return true;
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err);
      if (onError) {
        onError(errorMsg);
      }
      return false;
    }
  }

  public showRewarded(
    rewardType: RewardRewardType,
    onRewardGranted: (rewardType: RewardRewardType) => void,
    onDismissed?: AdEventCallback,
    onError?: AdErrorCallback
  ): void {
    if (!this.initialized || this.rewardedQueue.length === 0) {
      if (onError) {
        onError('Rewarded video ad is not ready yet. Please try again shortly.');
      }
      return;
    }

    try {
      console.log(`[AdMobService] Presenting Rewarded Video Ad for reward intent: ${rewardType}`);
      // Dequeue preloaded rewarded ad
      this.rewardedQueue.shift();

      // Trigger reward callback
      onRewardGranted(rewardType);

      if (onDismissed) {
        onDismissed();
      }

      // Preload next rewarded video
      this.preloadRewarded();
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err);
      if (onError) {
        onError(errorMsg);
      }
    }
  }

  public recordInterstitialImpression(timestamp: number = Date.now()): void {
    this.lastInterstitialShownTimestamp = timestamp;
  }

  public getLastInterstitialShownTimestamp(): number {
    return this.lastInterstitialShownTimestamp;
  }

  public getInterstitialQueueLength(): number {
    return this.interstitialQueue.length;
  }

  public getRewardedQueueLength(): number {
    return this.rewardedQueue.length;
  }
}

// Export singleton facade helper
export const adMobService: IAdMobService = AdMobService.getInstance();
