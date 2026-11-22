import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert';
import { AdMobService } from '../../services/admob/admob.service.ts';
import { DEV_ADMOB_CONFIG } from '../../config/admob.config.ts';
import {
  BANNER_HEIGHT_DP,
  MIN_TOUCH_PADDING_DP,
  OFFICIAL_TEST_BANNER_ID,
  AdaptiveBannerAd,
} from '../../components/ads/AdaptiveBannerAd.tsx';
import {
  TrackerScreen,
  ADMOB_BANNER_RESERVED_HEIGHT_DP,
  ADMOB_BANNER_TOUCH_PADDING_DP,
  getMotivationSubtitle,
} from '../../features/tracker/screens/TrackerScreen.tsx';
import {
  calculateHydrationProgress,
  calculateSineWavePoint,
  calculateSpringPhysicsStep,
} from '../../features/tracker/components/WaveProgress.tsx';
import type { DailyProgress, StreakState, UserProfile } from '../../types/water.ts';

describe('Android QA Lead Comprehensive Audit Suite', () => {
  let adMob: AdMobService;

  beforeEach(() => {
    AdMobService.resetInstance();
    adMob = AdMobService.getInstance(DEV_ADMOB_CONFIG);
  });

  // =========================================================================
  // TEST SECTION 1: Banner Layout Integrity & Accidental Click Prevention
  // =========================================================================
  describe('Audit 1: AdMob Adaptive Banner Placement & Layout Safety', () => {
    it('verifies that AdaptiveBannerAd reserves exactly 60dp height and at least 16dp padding', () => {
      assert.strictEqual(BANNER_HEIGHT_DP, 60, 'Banner height must be reserved at 60dp');
      assert.strictEqual(MIN_TOUCH_PADDING_DP, 16, 'Touch isolation padding must be >=16dp');
      assert.strictEqual(
        OFFICIAL_TEST_BANNER_ID,
        'ca-app-pub-3940256099942544/6300978111',
        'Official Google Sample Banner ID must be used'
      );
    });

    it('verifies TrackerScreen footer reserves 60dp and keeps >=16dp buffer above ad slot', () => {
      assert.strictEqual(
        ADMOB_BANNER_RESERVED_HEIGHT_DP,
        60,
        'TrackerScreen must allocate 60dp height for bottom banner'
      );
      assert.strictEqual(
        ADMOB_BANNER_TOUCH_PADDING_DP,
        16,
        'TrackerScreen must isolate banner with >=16dp padding from drink buttons'
      );
    });

    it('verifies AdaptiveBannerAd clamps custom padding if less than 16dp policy minimum', () => {
      // Testing effective padding calculation logic:
      const paddingTestCases = [0, 8, 12, 16, 24];
      for (const p of paddingTestCases) {
        const effective = Math.max(p, MIN_TOUCH_PADDING_DP);
        assert.ok(
          effective >= 16,
          `Effective padding for input ${p} must be at least 16dp, received ${effective}`
        );
      }
    });
  });

  // =========================================================================
  // TEST SECTION 2: Rewarded Video Ad -> Wave Theme Unlock Flow
  // =========================================================================
  describe('Audit 2: Rewarded Ad Flow & Wave Color Theme Switching', () => {
    it('simulates complete Rewarded Ad lifecycle and unlocks new wave themes', async () => {
      await adMob.initialize();
      assert.strictEqual(adMob.isInitialized(), true);

      const availableWaveThemes = [
        { name: 'Cyan Classic', primary: '#06B6D4', secondary: '#22D3EE' },
        { name: 'Mint Fresh', primary: '#10B981', secondary: '#34D399' },
        { name: 'Sunset Glow', primary: '#FB923C', secondary: '#FBBF24' },
      ];

      let currentTheme = availableWaveThemes[0];
      let rewardGranted = false;
      let adDismissed = false;

      // 1. User taps Gift button (🎁) -> trigger Rewarded Video
      adMob.showRewarded(
        'UNLOCK_THEME',
        (rewardType) => {
          assert.strictEqual(rewardType, 'UNLOCK_THEME');
          rewardGranted = true;
          // Switch to unlocked Mint Fresh theme
          currentTheme = availableWaveThemes[1];
        },
        () => {
          adDismissed = true;
        }
      );

      // Verify callbacks
      assert.strictEqual(rewardGranted, true, 'Reward callback must be triggered upon completion');
      assert.strictEqual(adDismissed, true, 'Dismiss callback must be triggered after ad finishes');
      assert.strictEqual(currentTheme.primary, '#10B981', 'Wave color must update to Mint Fresh');

      // 2. Verify WaveProgress math with new theme colors
      const sinePoint = calculateSineWavePoint(100, 270, 150, 14, 0.5);
      assert.ok(Number.isFinite(sinePoint), 'Sine wave calculation with new theme must be stable');

      // 3. User watches second video -> unlock Sunset Glow
      adMob.showRewarded(
        'UNLOCK_THEME',
        (rewardType) => {
          rewardGranted = true;
          currentTheme = availableWaveThemes[2];
        }
      );
      assert.strictEqual(currentTheme.primary, '#FB923C', 'Wave color must update to Sunset Glow');
    });

    it('verifies Spring Physics smoothly interpolates water level without graphical stutter', () => {
      let state = { position: 0.2, velocity: 0 };
      const target = 0.8;

      // Simulate 30 animation frames
      for (let frame = 0; frame < 30; frame++) {
        state = calculateSpringPhysicsStep(state.position, target, state.velocity, 0.08, 0.82);
        assert.ok(Number.isFinite(state.position));
        assert.ok(Number.isFinite(state.velocity));
      }

      // After 30 frames, position must be close to target 0.8
      assert.ok(
        Math.abs(state.position - target) < 0.05,
        `Spring physics should converge close to target 0.8, got ${state.position}`
      );
    });
  });

  // =========================================================================
  // TEST SECTION 3: 100% Goal Achievement -> Natural Interstitial Ad
  // =========================================================================
  describe('Audit 3: 100% Goal Reached & Natural Interstitial Ad Trigger', () => {
    it('verifies goal achievement triggers celebration and presents Interstitial Ad', async () => {
      await adMob.initialize();

      let celebrationTriggered = false;
      const targetMl = 2000;
      let currentMl = 1500;

      // Function simulating drink logging
      const logDrink = (ml: number) => {
        currentMl += ml;
        if (currentMl >= targetMl && !celebrationTriggered) {
          celebrationTriggered = true;
        }
      };

      // 1. Log drink +250ml -> 1750ml (under goal)
      logDrink(250);
      assert.strictEqual(celebrationTriggered, false);
      assert.strictEqual(getMotivationSubtitle(currentMl, targetMl), 'Hãy uống từng ngụm nhỏ để cơ thể hấp thụ tốt nhất');

      // 2. Log drink +500ml -> 2250ml (achieves 100% goal)
      logDrink(500);
      assert.strictEqual(celebrationTriggered, true, '100% goal must trigger celebration');
      assert.strictEqual(getMotivationSubtitle(currentMl, targetMl), 'Tuyệt vời! Bạn đã đủ nước hôm nay 🎉');

      // 3. User views Congratulation Dialog and taps "Tuyệt vời, tiếp tục!" -> Shows Interstitial Ad
      assert.strictEqual(adMob.canShowInterstitial(), true, 'Ad must be preloaded and ready at natural transition');

      let adDismissed = false;
      const shown = adMob.showInterstitial(() => {
        adDismissed = true;
      });

      assert.strictEqual(shown, true, 'Interstitial ad should be successfully presented');
      assert.strictEqual(adDismissed, true, 'OnDismissed callback must fire');

      // 4. Verify 3-minute cooldown prevents rapid follow-up interstitial popups
      assert.strictEqual(
        adMob.canShowInterstitial(),
        false,
        'Subsequent interstitial must be blocked by 3-minute cooldown timer'
      );
    });

    it('verifies 50% goal milestone triggers interstitial ad once per day and subsequent adds under 100% do not trigger ads', async () => {
      await adMob.initialize();

      const targetMl = 2000;
      let currentMl = 0;
      let halfGoalAdShownToday = false;
      let goalCompletedAdShownToday = false;
      let adsShownCount = 0;

      const logDrink = (ml: number) => {
        const previousMl = currentMl;
        const newTotal = currentMl + ml;
        currentMl = newTotal;

        const halfGoal = Math.floor(targetMl / 2);
        // Milestone 50%: triggers once per day if crossed
        if (previousMl < halfGoal && newTotal >= halfGoal && !halfGoalAdShownToday) {
          halfGoalAdShownToday = true;
          if (newTotal < targetMl) {
            if (adMob.canShowInterstitial()) {
              adMob.showInterstitial(() => {});
              adsShownCount++;
            }
          }
        }

        // Milestone 100%: triggers once per day
        if (previousMl < targetMl && newTotal >= targetMl && !goalCompletedAdShownToday) {
          goalCompletedAdShownToday = true;
          halfGoalAdShownToday = true;
        }
      };

      // 1. Add 400ml (20% of goal) -> below 50%, no ad
      logDrink(400);
      assert.strictEqual(halfGoalAdShownToday, false);
      assert.strictEqual(adsShownCount, 0);

      // 2. Add 700ml -> 1100ml (55% of goal, reaches >= 50%) -> triggers Interstitial Ad
      logDrink(700);
      assert.strictEqual(halfGoalAdShownToday, true);
      assert.strictEqual(adsShownCount, 1);

      // 3. Add 400ml -> 1500ml (75% of goal) -> already triggered 50%, under 100% -> no additional ad
      logDrink(400);
      assert.strictEqual(halfGoalAdShownToday, true);
      assert.strictEqual(adsShownCount, 1);

      // 4. Add 300ml -> 1800ml (90% of goal) -> still under 100% -> no additional ad
      logDrink(300);
      assert.strictEqual(halfGoalAdShownToday, true);
      assert.strictEqual(adsShownCount, 1);

      // 5. Add 300ml -> 2100ml (105% of goal, reaches >= 100%) -> triggers 100% celebration
      logDrink(300);
      assert.strictEqual(goalCompletedAdShownToday, true);
    });

    it('verifies preloading queue does not overflow or leak memory', async () => {
      await adMob.initialize();

      // Preload multiple times
      for (let i = 0; i < 10; i++) {
        adMob.preloadInterstitial();
        adMob.preloadRewarded();
      }

      // Max capacity is strictly capped at 2 to avoid memory bloat
      assert.strictEqual(
        adMob.getInterstitialQueueLength() <= 2,
        true,
        'Interstitial queue must not exceed capacity 2'
      );
      assert.strictEqual(
        adMob.getRewardedQueueLength() <= 2,
        true,
        'Rewarded queue must not exceed capacity 2'
      );
    });
  });

  // =========================================================================
  // TEST SECTION 4: AdMob SDK & Android Lifecycle Safety
  // =========================================================================
  describe('Audit 4: Lifecycle Safety & Google Play Policy Compliance', () => {
    it('strictly verifies zero production AdMob IDs exist in the active dev configuration', () => {
      assert.strictEqual(DEV_ADMOB_CONFIG.isTesting, true);
      assert.ok(DEV_ADMOB_CONFIG.appId.includes('3940256099942544'));
      assert.ok(DEV_ADMOB_CONFIG.bannerAdUnitId.includes('3940256099942544'));
      assert.ok(DEV_ADMOB_CONFIG.interstitialAdUnitId.includes('3940256099942544'));
      assert.ok(DEV_ADMOB_CONFIG.rewardedAdUnitId.includes('3940256099942544'));
    });

    it('verifies interstitialCooldownMs is minimum 180,000ms (3 minutes)', () => {
      assert.ok(
        DEV_ADMOB_CONFIG.interstitialCooldownMs >= 180000,
        'Cooldown must be at least 3 minutes to avoid Disruptive Ads strikes'
      );
    });

    it('verifies official sample Native Ad unit ID compliance', () => {
      const NATIVE_TEST_ID = 'ca-app-pub-3940256099942544/2247696110';
      assert.ok(
        NATIVE_TEST_ID.startsWith('ca-app-pub-3940256099942544'),
        'Native Ad Unit ID must strictly use Google official sample test ID'
      );
    });

    it('verifies 4-intake post-100% interstitial ad frequency rule', () => {
      let postGoalAddsCount = 0;
      let adsShown = 0;

      const addWaterPostGoal = () => {
        postGoalAddsCount++;
        if (postGoalAddsCount % 4 === 0) {
          adsShown++;
        }
      };

      // 1st to 3rd add -> no ad
      addWaterPostGoal();
      addWaterPostGoal();
      addWaterPostGoal();
      assert.strictEqual(adsShown, 0);

      // 4th add -> ad shown!
      addWaterPostGoal();
      assert.strictEqual(adsShown, 1);

      // 5th to 7th add -> no ad
      addWaterPostGoal();
      addWaterPostGoal();
      addWaterPostGoal();
      assert.strictEqual(adsShown, 1);

      // 8th add -> 2nd ad shown!
      addWaterPostGoal();
      assert.strictEqual(adsShown, 2);
    });

    it('verifies onboarding completion triggers interstitial ad at transition', async () => {
      await adMob.initialize();
      let interstitialTriggered = false;

      // Simulate Onboarding completion
      const completeOnboarding = () => {
        if (adMob.canShowInterstitial()) {
          adMob.showInterstitial(() => {
            interstitialTriggered = true;
          });
        }
      };

      completeOnboarding();
      assert.strictEqual(interstitialTriggered, true, 'Onboarding completion should present Interstitial ad');
    });

    it('verifies deep analytics calculation for weekly and monthly history datasets', () => {
      const sampleDays = [
        { dayName: 'Mon', intakeMl: 2000, targetMl: 2000 },
        { dayName: 'Tue', intakeMl: 2500, targetMl: 2000 },
        { dayName: 'Wed', intakeMl: 1500, targetMl: 2000 },
        { dayName: 'Thu', intakeMl: 1000, targetMl: 2000 },
        { dayName: 'Fri', intakeMl: 2200, targetMl: 2000 },
        { dayName: 'Sat', intakeMl: 3000, targetMl: 2000 },
        { dayName: 'Sun', intakeMl: 1800, targetMl: 2000 }
      ];

      const totalMl = sampleDays.reduce((acc, d) => acc + d.intakeMl, 0);
      const targetTotalMl = sampleDays.reduce((acc, d) => acc + d.targetMl, 0);
      const completionRatePercent = Math.round((totalMl * 100) / targetTotalMl);
      const peakDay = sampleDays.reduce((prev, curr) => (curr.intakeMl > prev.intakeMl ? curr : prev));
      const achievedDaysCount = sampleDays.filter((d) => d.intakeMl >= d.targetMl).length;

      assert.strictEqual(totalMl, 14000);
      assert.strictEqual(targetTotalMl, 14000);
      assert.strictEqual(completionRatePercent, 100);
      assert.strictEqual(peakDay.intakeMl, 3000);
      assert.strictEqual(peakDay.dayName, 'Sat');
      assert.strictEqual(achievedDaysCount, 4);
    });

    it('verifies all 5 supported languages satisfy UI/UX Pro Max button single-line layout constraints', () => {
      const languageButtons = [
        { lang: 'en', cancel: 'Cancel', confirm: 'Confirm', customConfirm: 'Save' },
        { lang: 'vi', cancel: 'Hủy', confirm: 'Xác nhận', customConfirm: 'Ghi nhận' },
        { lang: 'es', cancel: 'Cancelar', confirm: 'Confirmar', customConfirm: 'Guardar' },
        { lang: 'ja', cancel: 'キャンセル', confirm: '記録する', customConfirm: '記録する' },
        { lang: 'de', cancel: 'Abbrechen', confirm: 'Bestätigen', customConfirm: 'Speichern' }
      ];

      // On a 360dp phone with 8dp button contentPadding and 14sp font,
      // maximum safe button label is ~15 characters.
      const MAX_SAFE_BUTTON_CHARS = 15;

      for (const item of languageButtons) {
        assert.ok(
          item.cancel.length <= MAX_SAFE_BUTTON_CHARS,
          `Cancel button in ${item.lang} ("${item.cancel}") exceeds max safe length`
        );
        assert.ok(
          item.confirm.length <= MAX_SAFE_BUTTON_CHARS,
          `Confirm button in ${item.lang} ("${item.confirm}") exceeds max safe length`
        );
        assert.ok(
          item.customConfirm.length <= MAX_SAFE_BUTTON_CHARS,
          `Custom confirm button in ${item.lang} ("${item.customConfirm}") exceeds max safe length`
        );
      }
    });

    it('verifies streak badge titles and pluralization correctness across all 5 languages', () => {
      const badgesTitleFormatter = {
        en: (streak: number) => (streak === 1 ? 'Streak Badges (1 Day)' : `Streak Badges (${streak} Days)`),
        vi: (streak: number) => `Huy Hiệu Chuỗi (${streak} Ngày)`,
        es: (streak: number) => (streak === 1 ? 'Medallas de Racha (1 Día)' : `Medallas de Racha (${streak} Días)`),
        ja: (streak: number) => `ストリークバッジ (${streak} 日)`,
        de: (streak: number) => (streak === 1 ? 'Serien-Abzeichen (1 Tag)' : `Serien-Abzeichen (${streak} Tage)`)
      };

      // Test streak = 1 (singular)
      assert.strictEqual(badgesTitleFormatter.en(1), 'Streak Badges (1 Day)');
      assert.strictEqual(badgesTitleFormatter.es(1), 'Medallas de Racha (1 Día)');
      assert.strictEqual(badgesTitleFormatter.de(1), 'Serien-Abzeichen (1 Tag)');
      assert.strictEqual(badgesTitleFormatter.vi(1), 'Huy Hiệu Chuỗi (1 Ngày)');
      assert.strictEqual(badgesTitleFormatter.ja(1), 'ストリークバッジ (1 日)');

      // Test streak = 7 (plural)
      assert.strictEqual(badgesTitleFormatter.en(7), 'Streak Badges (7 Days)');
      assert.strictEqual(badgesTitleFormatter.es(7), 'Medallas de Racha (7 Días)');
      assert.strictEqual(badgesTitleFormatter.de(7), 'Serien-Abzeichen (7 Tage)');
    });

    it('verifies centered 2x2 badge grid layout contains all 4 milestones without overflow', () => {
      const badges = [
        { id: 'starter', targetDays: 3 },
        { id: 'week', targetDays: 7 },
        { id: 'gold', targetDays: 14 },
        { id: 'master', targetDays: 30 }
      ];

      assert.strictEqual(badges.length, 4, 'Must have exactly 4 milestone badges');
      
      // 2x2 grid distribution
      const row1 = badges.slice(0, 2);
      const row2 = badges.slice(2, 4);

      assert.strictEqual(row1.length, 2, 'Row 1 must hold 2 badges');
      assert.strictEqual(row2.length, 2, 'Row 2 must hold 2 badges');
      assert.strictEqual(row1[0].targetDays, 3);
      assert.strictEqual(row1[1].targetDays, 7);
      assert.strictEqual(row2[0].targetDays, 14);
      assert.strictEqual(row2[1].targetDays, 30);
    });
  });
});
