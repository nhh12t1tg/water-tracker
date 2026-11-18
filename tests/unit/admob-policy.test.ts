import { register } from 'node:module';
register(new URL('../test-loader.mjs', import.meta.url));

import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert';
import { DEV_ADMOB_CONFIG, PROD_ADMOB_CONFIG } from '../../config/admob.config';
import { AdMobService } from '../../services/admob/admob.service';

describe('AdMob Policy & Cooldown Enforcement Unit Tests', () => {
  const GOOGLE_TEST_PREFIX = 'ca-app-pub-3940256099942544';

  describe('AdMob Test Unit ID Safety Mandate', () => {
    it('uses Google Official Sample Test IDs in DEV_ADMOB_CONFIG', () => {
      assert.ok(
        DEV_ADMOB_CONFIG.appId.startsWith(GOOGLE_TEST_PREFIX),
        `App ID must start with official sample test prefix: ${DEV_ADMOB_CONFIG.appId}`
      );
      assert.ok(
        DEV_ADMOB_CONFIG.bannerAdUnitId.startsWith(GOOGLE_TEST_PREFIX),
        `Banner Unit ID must use official sample test ID: ${DEV_ADMOB_CONFIG.bannerAdUnitId}`
      );
      assert.ok(
        DEV_ADMOB_CONFIG.interstitialAdUnitId.startsWith(GOOGLE_TEST_PREFIX),
        `Interstitial Unit ID must use official sample test ID: ${DEV_ADMOB_CONFIG.interstitialAdUnitId}`
      );
      assert.ok(
        DEV_ADMOB_CONFIG.rewardedAdUnitId.startsWith(GOOGLE_TEST_PREFIX),
        `Rewarded Video Unit ID must use official sample test ID: ${DEV_ADMOB_CONFIG.rewardedAdUnitId}`
      );
      assert.strictEqual(DEV_ADMOB_CONFIG.isTesting, true);
    });

    it('verifies DEV configuration matches exact Google Sample IDs', () => {
      assert.strictEqual(
        DEV_ADMOB_CONFIG.bannerAdUnitId,
        'ca-app-pub-3940256099942544/6300978111',
        'Must match standard Adaptive Banner test ID'
      );
      assert.strictEqual(
        DEV_ADMOB_CONFIG.interstitialAdUnitId,
        'ca-app-pub-3940256099942544/1033173712',
        'Must match standard Interstitial test ID'
      );
      assert.strictEqual(
        DEV_ADMOB_CONFIG.rewardedAdUnitId,
        'ca-app-pub-3940256099942544/5224354917',
        'Must match standard Rewarded Video test ID'
      );
    });

    it('enforces that testDeviceIds contains EMULATOR', () => {
      assert.ok(DEV_ADMOB_CONFIG.testDeviceIds.includes('EMULATOR'));
    });
  });

  describe('Review Focus 3: 3-Minute Cooldown & Interstitial Rapid-Tap Spam Protection', () => {
    let service: AdMobService;

    beforeEach(async () => {
      service = AdMobService.getInstance();
      await service.initialize();
    });

    it('enforces configured cooldown to be at least 3 minutes (180,000ms)', () => {
      assert.ok(
        DEV_ADMOB_CONFIG.interstitialCooldownMs >= 180000,
        `Cooldown must be >= 180000ms, currently: ${DEV_ADMOB_CONFIG.interstitialCooldownMs}ms`
      );
    });

    it('blocks interstitial when SDK is not initialized', () => {
      // Create fresh instance with test config where initialized = false
      const uninit = Object.create(AdMobService.prototype);
      Object.assign(uninit, {
        config: DEV_ADMOB_CONFIG,
        initialized: false,
        lastInterstitialShownTimestamp: 0,
        isInterstitialLoaded: false,
      });

      assert.strictEqual(uninit.canShowInterstitial(), false);
      let errorReceived = '';
      const shown = uninit.showInterstitial(undefined, (err: string) => {
        errorReceived = err;
      });
      assert.strictEqual(shown, false);
      assert.ok(errorReceived.includes('not ready or blocked'));
    });

    it('allows initial interstitial presentation, then blocks immediate follow-ups', () => {
      // Force ad to be ready and clear previous timestamp
      (service as any).isInterstitialLoaded = true;
      (service as any).lastInterstitialShownTimestamp = 0;

      assert.strictEqual(service.canShowInterstitial(), true);

      let dismissed = false;
      const firstResult = service.showInterstitial(() => {
        dismissed = true;
      });

      assert.strictEqual(firstResult, true, 'First interstitial should succeed');
      assert.strictEqual(dismissed, true, 'Dismiss callback should be called');

      // Immediate second call MUST fail due to active cooldown timer
      (service as any).isInterstitialLoaded = true; // even if ad loaded instantly
      assert.strictEqual(
        service.canShowInterstitial(),
        false,
        'canShowInterstitial must return false during active cooldown'
      );

      let errorMsg = '';
      const secondResult = service.showInterstitial(undefined, (err) => {
        errorMsg = err;
      });

      assert.strictEqual(secondResult, false, 'Second interstitial must be rejected');
      assert.ok(errorMsg.includes('frequency cooldown'), 'Error should specify cooldown block');
    });

    it('protects against rapid-tap spam (10 rapid consecutive clicks)', () => {
      // Reset state
      (service as any).isInterstitialLoaded = true;
      (service as any).lastInterstitialShownTimestamp = 0;

      let successCount = 0;
      let blockedCount = 0;

      // Simulate rapid user spam-tapping a button 10 times in milliseconds
      for (let i = 0; i < 10; i++) {
        // Even if native bridge had preloaded ads
        (service as any).isInterstitialLoaded = true;

        const result = service.showInterstitial(
          () => {},
          () => {
            blockedCount++;
          }
        );

        if (result) {
          successCount++;
        }
      }

      assert.strictEqual(successCount, 1, 'Only exactly 1 ad impression allowed during spam');
      assert.strictEqual(blockedCount, 9, 'Remaining 9 spam taps must be blocked by cooldown');
    });

    it('allows interstitial again after cooldown window has elapsed', () => {
      // Record an impression 5 minutes ago (300,000ms ago)
      const fiveMinutesAgo = Date.now() - 300000;
      (service as any).lastInterstitialShownTimestamp = fiveMinutesAgo;
      (service as any).isInterstitialLoaded = true;

      assert.strictEqual(
        service.canShowInterstitial(),
        true,
        'Should allow ad when cooldown has expired'
      );

      let dismissed = false;
      const result = service.showInterstitial(() => {
        dismissed = true;
      });

      assert.strictEqual(result, true);
      assert.strictEqual(dismissed, true);
    });
  });

  describe('Rewarded Video Ad Handling', () => {
    let service: AdMobService;

    beforeEach(async () => {
      service = AdMobService.getInstance();
      await service.initialize();
    });

    it('grants reward for FREEZE_STREAK upon complete watch', () => {
      let grantedType: string | null = null;
      let dismissed = false;

      (service as any).isRewardedLoaded = true;

      service.showRewarded(
        'FREEZE_STREAK',
        (type) => {
          grantedType = type;
        },
        () => {
          dismissed = true;
        }
      );

      assert.strictEqual(grantedType, 'FREEZE_STREAK');
      assert.strictEqual(dismissed, true);
    });

    it('grants reward for UNLOCK_THEME upon complete watch', () => {
      let grantedType: string | null = null;
      (service as any).isRewardedLoaded = true;

      service.showRewarded('UNLOCK_THEME', (type) => {
        grantedType = type;
      });

      assert.strictEqual(grantedType, 'UNLOCK_THEME');
    });

    it('reports error when rewarded ad is not yet loaded', () => {
      (service as any).isRewardedLoaded = false;

      let errorMsg = '';
      service.showRewarded(
        'FREEZE_STREAK',
        () => {},
        undefined,
        (err) => {
          errorMsg = err;
        }
      );

      assert.ok(errorMsg.includes('not ready yet'));
    });
  });
});
