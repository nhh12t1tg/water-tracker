package com.watertracker.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdMobManager - Centralized manager for AdMob Test Ads execution.
 * 
 * STRICT COMPLIANCE:
 * - 100% official Google sample test ad unit IDs.
 * - 3-minute (180,000ms) cooldown between Interstitial displays.
 * - Preloaded Ad queues to eliminate latency.
 */
object AdMobManager {

    private const val TAG = "AdMobManager"

    // Official Google Sample Test Ad Unit IDs
    const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"
    const val NATIVE_TEST_ID = "ca-app-pub-3940256099942544/2247696110"

    private const val INTERSTITIAL_COOLDOWN_MS = 180000L // 3 minutes

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShownTime: Long = 0L

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    /**
     * Preload Interstitial and Rewarded Ads at application startup.
     */
    fun initialize(context: Context) {
        preloadInterstitial(context)
        preloadRewarded(context)
    }

    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            INTERSTITIAL_TEST_ID,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Test InterstitialAd loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "InterstitialAd failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Show Interstitial Ad upon natural break (e.g., 100% daily goal achieved).
     * Strictly enforces 3-minute cooldown.
     */
    fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        val now = System.currentTimeMillis()
        val timeSinceLast = now - lastInterstitialShownTime

        if (timeSinceLast < INTERSTITIAL_COOLDOWN_MS && lastInterstitialShownTime > 0) {
            val remainingSec = (INTERSTITIAL_COOLDOWN_MS - timeSinceLast) / 1000
            Log.d(TAG, "Interstitial blocked by cooldown. $remainingSec seconds remaining.")
            onDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "InterstitialAd dismissed by user.")
                    interstitialAd = null
                    lastInterstitialShownTime = System.currentTimeMillis()
                    preloadInterstitial(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "InterstitialAd failed to show: ${adError.message}")
                    interstitialAd = null
                    preloadInterstitial(activity)
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "InterstitialAd displayed on screen.")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "InterstitialAd not ready yet. Continuing without blocking user.")
            preloadInterstitial(activity)
            onDismissed()
        }
    }

    fun preloadRewarded(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context.applicationContext,
            REWARDED_TEST_ID,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Test RewardedAd loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "RewardedAd failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Show Rewarded Video Ad when user opts in (Theme unlock or Streak freeze).
     */
    fun showRewarded(
        activity: Activity,
        onRewardGranted: () -> Unit,
        onDismissed: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad != null) {
            var rewardEarned = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "RewardedAd dismissed. Reward earned: $rewardEarned")
                    rewardedAd = null
                    preloadRewarded(activity)
                    if (rewardEarned) {
                        onRewardGranted()
                    }
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "RewardedAd failed to show: ${adError.message}")
                    rewardedAd = null
                    preloadRewarded(activity)
                    // Grant reward as fallback so test user isn't stuck
                    onRewardGranted()
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "RewardedAd displayed on screen.")
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.type} amount=${rewardItem.amount}")
                rewardEarned = true
            }
        } else {
            Log.d(TAG, "RewardedAd not ready yet. Preloading and granting fallback reward.")
            preloadRewarded(activity)
            onRewardGranted()
            onDismissed()
        }
    }

    /**
     * Load Google AdMob Native Ad using official test ID.
     */
    fun loadNativeAd(
        context: Context,
        adUnitId: String = NATIVE_TEST_ID,
        onAdLoaded: (NativeAd) -> Unit,
        onAdFailed: (LoadAdError) -> Unit = {}
    ) {
        val adLoader = AdLoader.Builder(context.applicationContext, adUnitId)
            .forNativeAd { ad ->
                Log.d(TAG, "NativeAd loaded successfully.")
                onAdLoaded(ad)
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "NativeAd failed to load: ${error.message}")
                    onAdFailed(error)
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder().build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
}
