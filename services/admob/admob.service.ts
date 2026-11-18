import {
  IAdMobService,
  AdMobConfig,
  RewardRewardType,
  AdEventCallback,
  AdErrorCallback,
} from '../../types/water';
import { getActiveAdMobConfig } from '../../config/admob.config';

/**
 * AdMobService - Singleton Facade implementation of IAdMobService.
 * 
 * Architectural Safety Rules:
 * 1. Screens and ViewModels never directly instantiate Ad SDK objects.
 * 2. Interstitials are strictly frequency-capped by cooldown timers.
 * 3. Accidental clicks are prevented by maintaining clear touch boundaries.
 */
export class AdMobService implements IAdMobService {
  private static instance: AdMobService | null = null;
  private readonly config: AdMobConfig;
  private initialized: boolean = false;
  private lastInterstitialShownTimestamp: number = 0;
  private isInterstitialLoaded: boolean = false;
  private isRewardedLoaded: boolean = false;

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

  public async initialize(): Promise<void> {
    if (this.initialized) return;

    // Simulate SDK initialization logic / native bridge call
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
    if (!this.initialized) return;
    console.log(`[AdMobService] Preloading Interstitial ad: ${this.config.interstitialAdUnitId}`);
    // Native bridge / SDK call would load the ad here
    this.isInterstitialLoaded = true;
  }

  public preloadRewarded(): void {
    if (!this.initialized) return;
    console.log(`[AdMobService] Preloading Rewarded ad: ${this.config.rewardedAdUnitId}`);
    // Native bridge / SDK call would load the ad here
    this.isRewardedLoaded = true;
  }

  public canShowInterstitial(): boolean {
    if (!this.initialized || !this.isInterstitialLoaded) {
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
      if (onError) onError('Interstitial ad not ready or blocked by frequency cooldown.');
      return false;
    }

    try {
      console.log(`[AdMobService] Presenting Interstitial Ad at natural transition.`);
      this.recordInterstitialImpression();
      this.isInterstitialLoaded = false;

      // In real implementation, bind to native ad dismissal callback
      if (onDismissed) onDismissed();

      // Immediately queue preload for the next opportunity
      this.preloadInterstitial();
      return true;
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err);
      if (onError) onError(errorMsg);
      return false;
    }
  }

  public showRewarded(
    rewardType: RewardRewardType,
    onRewardGranted: (rewardType: RewardRewardType) => void,
    onDismissed?: AdEventCallback,
    onError?: AdErrorCallback
  ): void {
    if (!this.initialized || !this.isRewardedLoaded) {
      if (onError) onError('Rewarded video ad is not ready yet. Please try again shortly.');
      return;
    }

    try {
      console.log(`[AdMobService] Presenting Rewarded Video Ad for reward intent: ${rewardType}`);
      this.isRewardedLoaded = false;

      // Simulate reward grant upon complete watch
      onRewardGranted(rewardType);

      if (onDismissed) onDismissed();

      // Preload next rewarded video
      this.preloadRewarded();
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err);
      if (onError) onError(errorMsg);
    }
  }

  public recordInterstitialImpression(): void {
    this.lastInterstitialShownTimestamp = Date.now();
  }
}

// Export singleton factory helper
export const adMobService: IAdMobService = AdMobService.getInstance();
