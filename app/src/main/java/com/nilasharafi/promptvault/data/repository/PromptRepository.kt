package com.nilasharafi.promptvault.data.repository

import com.nilasharafi.promptvault.data.local.PromptDataStore
import com.nilasharafi.promptvault.data.model.AppConfig
import com.nilasharafi.promptvault.data.model.PromptItem
import com.nilasharafi.promptvault.data.model.PromptsResponse
import com.nilasharafi.promptvault.data.model.UnlockState
import com.nilasharafi.promptvault.data.remote.FetchResult
import com.nilasharafi.promptvault.data.remote.PromptApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class PromptRepository(
    private val apiService: PromptApiService,
    private val dataStore: PromptDataStore,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val _promptsState = MutableStateFlow<PromptsResponse>(PromptsResponse())
    val promptsState: StateFlow<PromptsResponse> = _promptsState.asStateFlow()

    private val _configState = MutableStateFlow<AppConfig>(AppConfig())
    val configState: StateFlow<AppConfig> = _configState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    init {
        // 1. Immediately load cached data from local DataStore
        scope.launch {
            loadFromCache()
            // 2. Fetch fresh data in the background
            refreshAll(force = false)
        }
    }

    private suspend fun loadFromCache() {
        val cachedPrompts = dataStore.cachedPromptsJson.first()
        if (!cachedPrompts.isNullOrBlank()) {
            try {
                val parsed = json.decodeFromString<PromptsResponse>(cachedPrompts)
                _promptsState.value = parsed
            } catch (_: Exception) { }
        }

        val cachedConfig = dataStore.cachedConfigJson.first()
        if (!cachedConfig.isNullOrBlank()) {
            try {
                val parsed = json.decodeFromString<AppConfig>(cachedConfig)
                _configState.value = parsed
            } catch (_: Exception) { }
        }
    }

    suspend fun refreshAll(force: Boolean = false): Boolean {
        _isRefreshing.value = true
        _lastError.value = null
        var anySuccess = false

        try {
            val promptsEtag = if (force) null else dataStore.getPromptsEtag()
            when (val result = apiService.fetchPrompts(promptsEtag, forceRefresh = force)) {
                is FetchResult.Success -> {
                    _promptsState.value = result.data
                    dataStore.savePromptsCache(result.rawJson, result.etag)
                    anySuccess = true
                }
                is FetchResult.NotModified -> {
                    anySuccess = true
                }
                is FetchResult.Error -> {
                    _lastError.value = result.message
                }
            }

            val configEtag = if (force) null else dataStore.getConfigEtag()
            when (val configResult = apiService.fetchAppConfig(configEtag, forceRefresh = force)) {
                is FetchResult.Success -> {
                    _configState.value = configResult.data
                    dataStore.saveConfigCache(configResult.rawJson, configResult.etag)
                    anySuccess = true
                }
                is FetchResult.NotModified -> {
                    anySuccess = true
                }
                is FetchResult.Error -> {
                    // Config fallback to cache
                }
            }
        } finally {
            _isRefreshing.value = false
        }
        return anySuccess
    }

    fun isPromptUnlocked(promptId: String, isPremium: Boolean): Flow<UnlockState> {
        return dataStore.isPromptUnlockedFlow(promptId, isPremium)
    }

    suspend fun unlockPrompt(promptId: String) {
        val config = _configState.value.premium
        dataStore.unlockPrompt(
            promptId = promptId,
            durationHours = config.unlockDurationHours,
            scope = config.unlockScope
        )
    }

    suspend fun getPromptById(id: String): PromptItem? {
        return _promptsState.value.items.find { it.id == id }
    }

    suspend fun getSecurePromptText(item: PromptItem, unlockState: UnlockState): String {
        return if (!item.isPremium || unlockState is UnlockState.Unlocked) {
            item.prompt
        } else {
            // Obfuscated teaser placeholder - real text is NEVER leaked when locked
            "🔒 Watch a short rewarded ad to unlock the complete prompt for ${item.title}."
        }
    }

    // Favorites
    val favoriteIds: Flow<Set<String>> = dataStore.favoriteIds

    suspend fun toggleFavorite(promptId: String): Boolean {
        return dataStore.toggleFavorite(promptId)
    }

    // Settings
    val themeMode: Flow<String> = dataStore.themeMode
    val language: Flow<String> = dataStore.language
    val autoRefresh: Flow<String> = dataStore.autoRefresh
    val wifiOnly: Flow<Boolean> = dataStore.wifiOnly
    val hasSeenOnboarding: Flow<Boolean> = dataStore.hasSeenOnboarding

    suspend fun setThemeMode(mode: String) = dataStore.setThemeMode(mode)
    suspend fun setLanguage(lang: String) = dataStore.setLanguage(lang)
    suspend fun setAutoRefresh(interval: String) = dataStore.setAutoRefresh(interval)
    suspend fun setWifiOnly(wifiOnly: Boolean) = dataStore.setWifiOnly(wifiOnly)
    suspend fun setOnboardingSeen(seen: Boolean) = dataStore.setOnboardingSeen(seen)
    suspend fun clearCache() = dataStore.clearCache()
}
