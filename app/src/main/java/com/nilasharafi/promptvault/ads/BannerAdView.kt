package com.nilasharafi.promptvault.ads

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.nilasharafi.promptvault.data.model.AdsConfig

@Composable
fun BannerAdView(
    adsConfig: AdsConfig,
    screenName: String,
    modifier: Modifier = Modifier
) {
    val bannerConfig = adsConfig.banner
    if (!adsConfig.masterEnabled || !bannerConfig.enabled || !bannerConfig.screens.contains(screenName)) {
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val unitId = remember(adsConfig) {
        AdManager.instance.getEffectiveUnitId(
            configuredId = bannerConfig.unitId,
            testId = AdManager.TEST_BANNER_ID,
            useTest = adsConfig.useTestAds
        )
    }

    val adView = remember(unitId) {
        AdView(context).apply {
            adUnitId = unitId
            val activity = context as? Activity
            val displayMetrics = context.resources.displayMetrics
            val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth))
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_DESTROY -> adView.destroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier.fillMaxWidth()
    )
}
