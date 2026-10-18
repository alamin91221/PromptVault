package com.nilasharafi.promptvault.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.nilasharafi.promptvault.BuildConfig
import com.nilasharafi.promptvault.data.model.AdsConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class AdManager private constructor() {

    private val isMobileAdsInitialized = AtomicBoolean(false)
    private var isRewardedFlowActive = false

    // Interstitial state
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var promptViewsCount = 0
    private var interstitialsShownSession = 0
    private var lastInterstitialShownTime = 0L

    // Rewarded state
    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    // Native ad pool
    private val nativeAdPool = mutableListOf<NativeAd>()
    private var isNativeLoading = false

    companion object {
        const val TAG = "PromptVault_AdManager"
        val instance: AdManager by lazy { AdManager() }

        // Official Google Test Ad Unit IDs
        const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
        const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    }

    /**
     * Request UMP Consent and initialize Google Mobile Ads SDK on background thread
     */
    fun initializeWithConsent(
        activity: Activity,
        adsConfigFlow: StateFlow<AdsConfig>,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) {
        val params = ConsentRequestParameters.Builder().build()
        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)

        consentInfo.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.message}")
                    }
                    if (consentInfo.canRequestAds()) {
                        initMobileAds(activity.applicationContext, adsConfigFlow, scope)
                    }
                }
            },
            { reqError ->
                Log.w(TAG, "Consent request error: ${reqError.message}")
                // Fallback to init if offline/cached consent allows
                if (consentInfo.canRequestAds()) {
                    initMobileAds(activity.applicationContext, adsConfigFlow, scope)
                }
            }
        )

        // Listen for config changes to preload / refresh ads
        scope.launch {
            adsConfigFlow.collect { config ->
                if (isMobileAdsInitialized.get() && config.masterEnabled) {
                    preloadInterstitial(activity.applicationContext, config)
                    preloadRewarded(activity.applicationContext, config)
                    preloadNativeAd(activity.applicationContext, config)
                }
            }
        }
    }

    fun showPrivacyOptionsForm(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) {
                Log.w(TAG, "Privacy options form error: ${error.message}")
            }
        }
    }

    private fun initMobileAds(
        context: Context,
        adsConfigFlow: StateFlow<AdsConfig>,
        scope: CoroutineScope
    ) {
        if (isMobileAdsInitialized.getAndSet(true)) return

        scope.launch(Dispatchers.IO) {
            try {
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "MobileAds initialized: $status")
                    val config = adsConfigFlow.value
                    if (config.masterEnabled) {
                        preloadInterstitial(context, config)
                        preloadRewarded(context, config)
                        preloadNativeAd(context, config)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "MobileAds initialization error", e)
            }
        }
    }

    fun getEffectiveUnitId(configuredId: String, testId: String, useTest: Boolean): String {
        return if (BuildConfig.DEBUG || useTest || configuredId.isBlank()) testId else configuredId
    }

    // =========================================================================
    // INTERSTITIAL ADS
    // =========================================================================

    fun preloadInterstitial(context: Context, config: AdsConfig) {
        if (!config.masterEnabled || !config.interstitial.enabled || isInterstitialLoading || interstitialAd != null) {
            return
        }
        val unitId = getEffectiveUnitId(config.interstitial.unitId, TEST_INTERSTITIAL_ID, config.useTestAds)
        isInterstitialLoading = true

        val request = AdRequest.Builder().build()
        InterstitialAd.load(context, unitId, request, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                isInterstitialLoading = false
                Log.d(TAG, "Interstitial loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                isInterstitialLoading = false
                Log.w(TAG, "Interstitial failed to load: ${error.message}")
            }
        })
    }

    fun onPromptViewed(activity: Activity, config: AdsConfig) {
        promptViewsCount++
        val ic = config.interstitial
        if (!config.masterEnabled || !ic.enabled || isRewardedFlowActive) return

        // Skip first N actions rule
        if (promptViewsCount <= ic.skipForFirstNActions) return

        // Max per session rule
        if (interstitialsShownSession >= ic.maxPerSession) return

        // Show every N views rule
        if ((promptViewsCount - ic.skipForFirstNActions) % ic.showEveryNPromptViews != 0) return

        // Min interval rule
        val now = System.currentTimeMillis()
        if (now - lastInterstitialShownTime < ic.minIntervalSeconds * 1000L) return

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastInterstitialShownTime = System.currentTimeMillis()
                    interstitialsShownSession++
                    preloadInterstitial(activity.applicationContext, config)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    preloadInterstitial(activity.applicationContext, config)
                }
            }
            ad.show(activity)
        } else {
            preloadInterstitial(activity.applicationContext, config)
        }
    }

    // =========================================================================
    // REWARDED ADS
    // =========================================================================

    fun preloadRewarded(context: Context, config: AdsConfig) {
        if (!config.masterEnabled || !config.rewarded.enabled || isRewardedLoading || rewardedAd != null) {
            return
        }
        val unitId = getEffectiveUnitId(config.rewarded.unitId, TEST_REWARDED_ID, config.useTestAds)
        isRewardedLoading = true

        val request = AdRequest.Builder().build()
        RewardedAd.load(context, unitId, request, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isRewardedLoading = false
                Log.d(TAG, "Rewarded ad loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAd = null
                isRewardedLoading = false
                Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
            }
        })
    }

    fun showRewardedAd(
        activity: Activity,
        config: AdsConfig,
        onRewardEarned: () -> Unit,
        onAdFailed: (String) -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            preloadRewarded(activity.applicationContext, config)
            onAdFailed("Rewarded ad is buffering. Please try again in a few seconds.")
            return
        }

        isRewardedFlowActive = true
        var userEarned = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                isRewardedFlowActive = false
                preloadRewarded(activity.applicationContext, config)
                if (userEarned) {
                    onRewardEarned()
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                isRewardedFlowActive = false
                preloadRewarded(activity.applicationContext, config)
                onAdFailed(error.message)
            }
        }

        ad.show(activity) { _ ->
            userEarned = true
        }
    }

    // =========================================================================
    // NATIVE ADS
    // =========================================================================

    fun preloadNativeAd(context: Context, config: AdsConfig) {
        if (!config.masterEnabled || !config.native.enabled || isNativeLoading || nativeAdPool.size >= 3) {
            return
        }
        val unitId = getEffectiveUnitId(config.native.unitId, TEST_NATIVE_ID, config.useTestAds)
        isNativeLoading = true

        val loader = com.google.android.gms.ads.AdLoader.Builder(context, unitId)
            .forNativeAd { nativeAd ->
                nativeAdPool.add(nativeAd)
                isNativeLoading = false
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isNativeLoading = false
                }
            })
            .build()

        loader.loadAd(AdRequest.Builder().build())
    }

    fun acquireNativeAd(context: Context, config: AdsConfig): NativeAd? {
        val ad = if (nativeAdPool.isNotEmpty()) nativeAdPool.removeAt(0) else null
        preloadNativeAd(context, config)
        return ad
    }
}
