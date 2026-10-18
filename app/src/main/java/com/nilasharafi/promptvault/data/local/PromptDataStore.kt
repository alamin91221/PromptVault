package com.nilasharafi.promptvault.data.local

import android.content.Context
import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nilasharafi.promptvault.data.model.UnlockState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "promptvault_prefs")

class PromptDataStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        // Cache keys
        val KEY_PROMPTS_JSON = stringPreferencesKey("cached_prompts_json")
        val KEY_PROMPTS_ETAG = stringPreferencesKey("cached_prompts_etag")
        val KEY_CONFIG_JSON = stringPreferencesKey("cached_config_json")
        val KEY_CONFIG_ETAG = stringPreferencesKey("cached_config_etag")
        val KEY_LAST_CACHE_TIME = longPreferencesKey("last_cache_time")

        // Favorites
        val KEY_FAVORITES = stringSetPreferencesKey("favorite_prompt_ids")

        // Premium Unlocks
        val KEY_UNLOCKS_MAP_JSON = stringPreferencesKey("unlocks_map_json")
        val KEY_GLOBAL_UNLOCK_EXPIRES = longPreferencesKey("global_unlock_expires")
        val KEY_LAST_SYSTEM_TIME = longPreferencesKey("last_system_time")
        val KEY_LAST_ELAPSED_REALTIME = longPreferencesKey("last_elapsed_realtime")

        // Settings
        val KEY_THEME = stringPreferencesKey("setting_theme") // "system", "dark", "light"
        val KEY_LANGUAGE = stringPreferencesKey("setting_language") // "system", "en", "bn"
        val KEY_AUTO_REFRESH = stringPreferencesKey("setting_auto_refresh") // "1h", "6h", "manual"
        val KEY_WIFI_ONLY = booleanPreferencesKey("setting_wifi_only")
        val KEY_ONBOARDING_SEEN = booleanPreferencesKey("setting_onboarding_seen")
    }

    // Cache operations
    val cachedPromptsJson: Flow<String?> = context.dataStore.data.map { it[KEY_PROMPTS_JSON] }
    val cachedConfigJson: Flow<String?> = context.dataStore.data.map { it[KEY_CONFIG_JSON] }

    suspend fun savePromptsCache(jsonStr: String, etag: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PROMPTS_JSON] = jsonStr
            etag?.let { prefs[KEY_PROMPTS_ETAG] = it }
            prefs[KEY_LAST_CACHE_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun saveConfigCache(jsonStr: String, etag: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CONFIG_JSON] = jsonStr
            etag?.let { prefs[KEY_CONFIG_ETAG] = it }
        }
    }

    suspend fun getPromptsEtag(): String? = context.dataStore.data.map { it[KEY_PROMPTS_ETAG] }.first()
    suspend fun getConfigEtag(): String? = context.dataStore.data.map { it[KEY_CONFIG_ETAG] }.first()

    // Favorites
    val favoriteIds: Flow<Set<String>> = context.dataStore.data.map { it[KEY_FAVORITES] ?: emptySet() }

    suspend fun toggleFavorite(promptId: String): Boolean {
        var isFav = false
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(promptId)) {
                current.remove(promptId)
                isFav = false
            } else {
                current.add(promptId)
                isFav = true
            }
            prefs[KEY_FAVORITES] = current
        }
        return isFav
    }

    suspend fun isFavorite(promptId: String): Boolean {
        val favs = favoriteIds.first()
        return favs.contains(promptId)
    }

    // Premium Unlocks & Anti-Tamper Clock logic
    suspend fun unlockPrompt(promptId: String, durationHours: Int, scope: String) {
        val now = System.currentTimeMillis()
        val elapsed = SystemClock.elapsedRealtime()
        val durationMillis = TimeUnit.HOURS.toMillis(durationHours.toLong())
        val expiresAt = now + durationMillis

        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_SYSTEM_TIME] = now
            prefs[KEY_LAST_ELAPSED_REALTIME] = elapsed

            if (scope.equals("all", ignoreCase = true)) {
                prefs[KEY_GLOBAL_UNLOCK_EXPIRES] = expiresAt
            } else {
                val currentMap = loadUnlocksMap(prefs[KEY_UNLOCKS_MAP_JSON])
                currentMap[promptId] = expiresAt
                prefs[KEY_UNLOCKS_MAP_JSON] = json.encodeToString(
                    MapSerializer(String.serializer(), Long.serializer()),
                    currentMap
                )
            }
        }
    }

    fun isPromptUnlockedFlow(promptId: String, isPremium: Boolean): Flow<UnlockState> {
        if (!isPremium) return kotlinx.coroutines.flow.flowOf(UnlockState.Free)

        return context.dataStore.data.map { prefs ->
            val now = System.currentTimeMillis()
            val lastRecordedTime = prefs[KEY_LAST_SYSTEM_TIME] ?: now

            // Anti-tamper check: if clock rolled backwards significantly (> 5 mins)
            if (now < lastRecordedTime - 300_000L) {
                return@map UnlockState.Locked
            }

            // Check global unlock first
            val globalExpiry = prefs[KEY_GLOBAL_UNLOCK_EXPIRES] ?: 0L
            if (globalExpiry > now) {
                val remainingMillis = globalExpiry - now
                return@map UnlockState.Unlocked(
                    remainingFormatted = formatRemainingTime(remainingMillis),
                    expiresAtMillis = globalExpiry
                )
            }

            // Check single unlock
            val map = loadUnlocksMap(prefs[KEY_UNLOCKS_MAP_JSON])
            val expiry = map[promptId] ?: 0L
            if (expiry > now) {
                val remainingMillis = expiry - now
                UnlockState.Unlocked(
                    remainingFormatted = formatRemainingTime(remainingMillis),
                    expiresAtMillis = expiry
                )
            } else {
                UnlockState.Locked
            }
        }
    }

    private fun loadUnlocksMap(jsonStr: String?): MutableMap<String, Long> {
        if (jsonStr.isNullOrBlank()) return mutableMapOf()
        return try {
            json.decodeFromString(
                MapSerializer(String.serializer(), Long.serializer()),
                jsonStr
            ).toMutableMap()
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    private fun formatRemainingTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return if (hours >= 24) {
            val days = hours / 24
            val remHours = hours % 24
            "${days}d ${remHours}h"
        } else if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    // Settings
    val themeMode: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "system" }
    val language: Flow<String> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: "system" }
    val autoRefresh: Flow<String> = context.dataStore.data.map { it[KEY_AUTO_REFRESH] ?: "1h" }
    val wifiOnly: Flow<Boolean> = context.dataStore.data.map { it[KEY_WIFI_ONLY] ?: false }
    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_SEEN] ?: false }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME] = mode }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun setAutoRefresh(interval: String) {
        context.dataStore.edit { it[KEY_AUTO_REFRESH] = interval }
    }

    suspend fun setWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { it[KEY_WIFI_ONLY] = wifiOnly }
    }

    suspend fun setOnboardingSeen(seen: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_SEEN] = seen }
    }

    suspend fun clearCache() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_PROMPTS_JSON)
            prefs.remove(KEY_PROMPTS_ETAG)
            prefs.remove(KEY_CONFIG_JSON)
            prefs.remove(KEY_CONFIG_ETAG)
        }
    }
}
