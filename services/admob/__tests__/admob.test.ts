import { test, describe, beforeEach } from 'node:test';
import assert from 'node:assert';
import { DEV_ADMOB_CONFIG, PROD_ADMOB_CONFIG, getActiveAdMobConfig } from '../../../config/admob.config';
import { AdMobService } from '../admob.service';
import { RewardRewardType } from '../../../types/water';
import {
  AdaptiveBannerAd,
  BANNER_HEIGHT_DP,
  MIN_TOUCH_PADDING_DP,
  OFFICIAL_TEST_BANNER_ID,
} from '../../../components/ads/AdaptiveBannerAd';

describe('AdMob Configuration & Policy Compliance', () => {
  test('strictly enforces official Google Sample Test Ad Unit IDs', () => {
    assert.strictEqual(
      DEV_ADMOB_CONFIG.bannerAdUnitId,
      'ca-app-pub-3940256099942544/6300978111',
      'Banner ad unit ID must be official sample ID'
    );
    assert.strictEqual(
      DEV_ADMOB_CONFIG.interstitialAdUnitId,
      'ca-app-pub-3940256099942544/1033173712',
      'Interstitial ad unit ID must be official sample ID'
    );
    assert.strictEqual(
      DEV_ADMOB_CONFIG.rewardedAdUnitId,
      'ca-app-pub-3940256099942544/5224354917',
      'Rewarded ad unit ID must be official sample ID'
    );
    assert.strictEqual(DEV_ADMOB_CONFIG.isTesting, true);
  });

  test('strictly enforces 3-minute (180,000ms) cooldown for interstitial ads', () => {
    assert.strictEqual(
      DEV_ADMOB_CONFIG.interstitialCooldownMs,
      180000,
      'Interstitial cooldown must be exactly 180,000 ms (3 minutes)'
    );
    assert.strictEqual(
      PROD_ADMOB_CONFIG.interstitialCooldownMs,
      180000,
      'Prod Interstitial cooldown must also enforce 180,000 ms (3 minutes)'
    );
  });

  test('getActiveAdMobConfig returns dev config when NODE_ENV is not production', () => {
    const config = getActiveAdMobConfig();
    assert.strictEqual(config.isTesting, true);
    assert.strictEqual(config.interstitialCooldownMs, 180000);
  });
});

describe('AdMobService Preload Queues & Lifecycle', () => {
  beforeEach(() => {
    AdMobService.resetInstance();
  });

  test('implements singleton pattern', () => {
    const instance1 = AdMobService.getInstance();
    const instance2 = AdMobService.getInstance();
    assert.strictEqual(instance1, instance2);
  });

  test('initializes and populates preload queues', async () => {
    const service = AdMobService.getInstance();
    assert.strictEqual(service.isInitialized(), false);
    assert.strictEqual(service.getInterstitialQueueLength(), 0);
    assert.strictEqual(service.getRewardedQueueLength(), 0);

    await service.initialize();
    assert.strictEqual(service.isInitialized(), true);
    assert.ok(service.getInterstitialQueueLength() > 0, 'Interstitial queue should have preloaded ad');
    assert.ok(service.getRewardedQueueLength() > 0, 'Rewarded queue should have preloaded ad');
  });

  test('cannot show interstitial or rewarded before initialization', () => {
    const service = AdMobService.getInstance();
    assert.strictEqual(service.canShowInterstitial(), false);

    let interstitialError = '';
    const shown = service.showInterstitial(undefined, (err) => {
      interstitialError = err;
    });
    assert.strictEqual(shown, false);
    assert.ok(interstitialError.includes('not ready or blocked'));

    let rewardedError = '';
    service.showRewarded(
      'UNLOCK_THEME',
      () => {},
      undefined,
      (err) => {
        rewardedError = err;
      }
    );
    assert.ok(rewardedError.includes('not ready yet'));
  });
});

describe('AdMobService Interstitial 3-Minute Cooldown Timer', () => {
  beforeEach(() => {
    AdMobService.resetInstance();
  });

  test('allows first interstitial then blocks subsequent attempts within 180,000ms', async () => {
    const service = AdMobService.getInstance();
    await service.initialize();

    // 1. Initial state: No prior impressions, should be allowed
    assert.strictEqual(service.canShowInterstitial(), true, 'First interstitial should be allowed');

    let dismissed = false;
    const result = service.showInterstitial(() => {
      dismissed = true;
    });

    assert.strictEqual(result, true);
    assert.strictEqual(dismissed, true);
    assert.ok(service.getLastInterstitialShownTimestamp() > 0);

    // 2. Immediately after: blocked by cooldown
    assert.strictEqual(
      service.canShowInterstitial(),
      false,
      'Immediate subsequent call must be blocked by 3-minute cooldown'
    );

    let errorMsg = '';
    const blockedResult = service.showInterstitial(undefined, (err) => {
      errorMsg = err;
    });
    assert.strictEqual(blockedResult, false);
    assert.ok(errorMsg.length > 0);

    // 3. Simulate elapsed time of 2 minutes (120,000ms): still blocked
    const twoMinutesAgo = Date.now() - 120000;
    service.recordInterstitialImpression(twoMinutesAgo);
    assert.strictEqual(
      service.canShowInterstitial(),
      false,
      'Must still be blocked after only 2 minutes elapsed'
    );

    // 4. Simulate elapsed time of 3 minutes + 1ms (180,001ms): allowed
    const threeMinutesAgo = Date.now() - 180001;
    service.recordInterstitialImpression(threeMinutesAgo);
    assert.strictEqual(
      service.canShowInterstitial(),
      true,
      'Must be allowed once 180,000ms cooldown has expired'
    );
  });
});

describe('AdMobService Rewarded Ads & Reward Types', () => {
  beforeEach(() => {
    AdMobService.resetInstance();
  });

  test('rewards UNLOCK_THEME correctly and replenishes queue', async () => {
    const service = AdMobService.getInstance();
    await service.initialize();

    let grantedReward: RewardRewardType | null = null;
    let wasDismissed = false;

    service.showRewarded(
      'UNLOCK_THEME',
      (rewardType) => {
        grantedReward = rewardType;
      },
      () => {
        wasDismissed = true;
      }
    );

    assert.strictEqual(grantedReward, 'UNLOCK_THEME');
    assert.strictEqual(wasDismissed, true);
    assert.ok(service.getRewardedQueueLength() > 0, 'Rewarded queue should be replenished');
  });

  test('rewards FREEZE_STREAK correctly and replenishes queue', async () => {
    const service = AdMobService.getInstance();
    await service.initialize();

    let grantedReward: RewardRewardType | null = null;
    let wasDismissed = false;

    service.showRewarded(
      'FREEZE_STREAK',
      (rewardType) => {
        grantedReward = rewardType;
      },
      () => {
        wasDismissed = true;
      }
    );

    assert.strictEqual(grantedReward, 'FREEZE_STREAK');
    assert.strictEqual(wasDismissed, true);
    assert.ok(service.getRewardedQueueLength() > 0, 'Rewarded queue should be replenished');
  });
});

describe('AdaptiveBannerAd Safe Area & Policy Specification', () => {
  test('exports 60dp height and >=16dp padding constants', () => {
    assert.strictEqual(BANNER_HEIGHT_DP, 60, 'Banner height must be 60dp fixed');
    assert.strictEqual(MIN_TOUCH_PADDING_DP, 16, 'Safety touch padding must be at least 16dp');
    assert.strictEqual(OFFICIAL_TEST_BANNER_ID, 'ca-app-pub-3940256099942544/6300978111');
  });

  test('renders adaptive banner element with required height and padding props', () => {
    const element = AdaptiveBannerAd({
      isTest: true,
      paddingDp: 16,
      heightDp: 60,
    });

    assert.ok(element !== null && typeof element === 'object');
    // Verify properties of the rendered virtual element
    const props = (element as any).props;
    assert.strictEqual(props.className, 'admob-adaptive-banner-container');
    assert.strictEqual(props.style.paddingTop, '16px');

    const slotChild = props.children;
    assert.strictEqual(slotChild.props.style.height, '60px');
    assert.strictEqual(slotChild.props['data-ad-unit-id'], DEV_ADMOB_CONFIG.bannerAdUnitId);
    assert.strictEqual(slotChild.props['data-is-test'], true);
  });

  test('enforces minimum 16dp padding even if smaller padding is passed', () => {
    const element = AdaptiveBannerAd({
      paddingDp: 4, // Intentionally too small
    });

    const props = (element as any).props;
    // Should be clamped to MIN_TOUCH_PADDING_DP (16px)
    assert.strictEqual(props.style.paddingTop, '16px');
  });
});
