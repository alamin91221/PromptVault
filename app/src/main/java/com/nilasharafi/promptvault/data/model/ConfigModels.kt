package com.nilasharafi.promptvault.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    @SerialName("schema_version")
    val schemaVersion: Int = 1,
    val app: AppInfo = AppInfo(),
    val ads: AdsConfig = AdsConfig(),
    val premium: PremiumConfig = PremiumConfig()
)

@Serializable
data class AppInfo(
    @SerialName("maintenance_mode")
    val maintenanceMode: Boolean = false,
    @SerialName("maintenance_message")
    val maintenanceMessage: String = "",
    @SerialName("min_version_code")
    val minVersionCode: Int = 1,
    @SerialName("update_url")
    val updateUrl: String = "",
    @SerialName("force_update")
    val forceUpdate: Boolean = false
)

@Serializable
data class AdsConfig(
    @SerialName("master_enabled")
    val masterEnabled: Boolean = true,
    @SerialName("use_test_ads")
    val useTestAds: Boolean = true,
    val banner: BannerConfig = BannerConfig(),
    val interstitial: InterstitialConfig = InterstitialConfig(),
    val native: NativeAdConfig = NativeAdConfig(),
    val rewarded: RewardedConfig = RewardedConfig()
)

@Serializable
data class BannerConfig(
    val enabled: Boolean = true,
    @SerialName("unit_id")
    val unitId: String = "ca-app-pub-3940256099942544/6300978111",
    val screens: List<String> = listOf("home", "detail"),
    val position: String = "bottom"
)

@Serializable
data class InterstitialConfig(
    val enabled: Boolean = true,
    @SerialName("unit_id")
    val unitId: String = "ca-app-pub-3940256099942544/1033173712",
    @SerialName("show_every_n_prompt_views")
    val showEveryNPromptViews: Int = 3,
    @SerialName("min_interval_seconds")
    val minIntervalSeconds: Int = 60,
    @SerialName("max_per_session")
    val maxPerSession: Int = 8,
    @SerialName("skip_for_first_n_actions")
    val skipForFirstNActions: Int = 2
)

@Serializable
data class NativeAdConfig(
    val enabled: Boolean = true,
    @SerialName("unit_id")
    val unitId: String = "ca-app-pub-3940256099942544/2247696110",
    @SerialName("start_after_items")
    val startAfterItems: Int = 4,
    @SerialName("every_n_items")
    val everyNItems: Int = 7
)

@Serializable
data class RewardedConfig(
    val enabled: Boolean = true,
    @SerialName("unit_id")
    val unitId: String = "ca-app-pub-3940256099942544/5224354917"
)

@Serializable
data class PremiumConfig(
    @SerialName("unlock_duration_hours")
    val unlockDurationHours: Int = 48,
    @SerialName("unlock_scope")
    val unlockScope: String = "single", // "single" or "all"
    @SerialName("ads_required_per_unlock")
    val adsRequiredPerUnlock: Int = 1
)
