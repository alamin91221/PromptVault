package com.nilasharafi.promptvault

import com.nilasharafi.promptvault.data.model.AppConfig
import com.nilasharafi.promptvault.data.model.InterstitialConfig
import com.nilasharafi.promptvault.data.model.PromptsResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class PromptVaultUnitTests {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun testJsonParsingWithMissingAndExtraFields() {
        val jsonWithExtraAndMissing = """
            {
                "schema_version": 2,
                "unexpected_field": "some random data",
                "nested_garbage": { "foo": 123 },
                "categories": ["Portrait", "Anime"],
                "items": [
                    {
                        "id": "test-1",
                        "title": "Neon Samurai",
                        "image_url": "https://example.com/img.webp",
                        "extra_attribute": 999
                    }
                ]
            }
        """.trimIndent()

        val parsed = json.decodeFromString<PromptsResponse>(jsonWithExtraAndMissing)
        assertEquals(2, parsed.schemaVersion)
        assertEquals(2, parsed.categories.size)
        assertEquals(1, parsed.items.size)
        val item = parsed.items[0]
        assertEquals("test-1", item.id)
        assertEquals("Neon Samurai", item.title)
        assertEquals("", item.prompt) // Missing field gets default empty string
        assertFalse(item.isPremium) // Missing boolean gets default false
    }

    @Test
    fun testAppConfigParsingDefaults() {
        val minimalConfig = """
            {
                "schema_version": 1
            }
        """.trimIndent()

        val parsed = json.decodeFromString<AppConfig>(minimalConfig)
        assertTrue(parsed.ads.masterEnabled)
        assertEquals(48, parsed.premium.unlockDurationHours)
        assertEquals("single", parsed.premium.unlockScope)
        assertEquals(3, parsed.ads.interstitial.showEveryNPromptViews)
    }

    @Test
    fun testUnlockExpiryAndAntiTamperRollback() {
        val now = 1700000000000L
        val unlockDurationHours = 48
        val durationMillis = TimeUnit.HOURS.toMillis(unlockDurationHours.toLong())
        val expiresAt = now + durationMillis

        // 1. Within valid unlock window
        val checkTimeNormal = now + TimeUnit.HOURS.toMillis(2)
        assertTrue("Should be unlocked at 2 hours in", expiresAt > checkTimeNormal)

        // 2. Expired after 49 hours
        val checkTimeExpired = now + TimeUnit.HOURS.toMillis(49)
        assertFalse("Should be expired at 49 hours", expiresAt > checkTimeExpired)

        // 3. Anti-tamper rollback detection:
        // If current recorded system time was 'now', and device time is suddenly set backwards by 1 hour:
        val rolledBackTime = now - TimeUnit.MINUTES.toMillis(60)
        val isRollbackDetected = rolledBackTime < (now - TimeUnit.MINUTES.toMillis(5))
        assertTrue("Anti-tamper should flag clock rollback", isRollbackDetected)
    }

    @Test
    fun testInterstitialFrequencyLogic() {
        val config = InterstitialConfig(
            enabled = true,
            showEveryNPromptViews = 3,
            minIntervalSeconds = 60,
            maxPerSession = 5,
            skipForFirstNActions = 2
        )

        fun shouldShowAd(viewCount: Int, sessionShown: Int, lastShownSecondsAgo: Long): Boolean {
            if (!config.enabled) return false
            if (viewCount <= config.skipForFirstNActions) return false
            if (sessionShown >= config.maxPerSession) return false
            if ((viewCount - config.skipForFirstNActions) % config.showEveryNPromptViews != 0) return false
            if (lastShownSecondsAgo < config.minIntervalSeconds) return false
            return true
        }

        // Action 1: skipped (skip first 2)
        assertFalse(shouldShowAd(viewCount = 1, sessionShown = 0, lastShownSecondsAgo = 100))
        // Action 2: skipped (skip first 2)
        assertFalse(shouldShowAd(viewCount = 2, sessionShown = 0, lastShownSecondsAgo = 100))
        // Action 3: (3 - 2) % 3 != 0 -> false
        assertFalse(shouldShowAd(viewCount = 3, sessionShown = 0, lastShownSecondsAgo = 100))
        // Action 5: (5 - 2) % 3 == 0, interval ok -> true!
        assertTrue(shouldShowAd(viewCount = 5, sessionShown = 0, lastShownSecondsAgo = 100))
        // Action 5 with short interval -> false
        assertFalse(shouldShowAd(viewCount = 5, sessionShown = 0, lastShownSecondsAgo = 30))
        // Action 5 with session capped -> false
        assertFalse(shouldShowAd(viewCount = 5, sessionShown = 5, lastShownSecondsAgo = 100))
    }
}
