package com.nilasharafi.promptvault.data.remote

import com.nilasharafi.promptvault.BuildConfig
import com.nilasharafi.promptvault.data.model.AppConfig
import com.nilasharafi.promptvault.data.model.PromptsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed interface FetchResult<out T> {
    data class Success<out T>(val data: T, val rawJson: String, val etag: String?) : FetchResult<T>
    data object NotModified : FetchResult<Nothing>
    data class Error(val exception: Throwable, val message: String) : FetchResult<Nothing>
}

class PromptApiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val baseUrl: String
        get() {
            var url = BuildConfig.BASE_URL
            if (!url.endsWith("/")) {
                url += "/"
            }
            return url
        }

    suspend fun fetchPrompts(etag: String? = null, forceRefresh: Boolean = false): FetchResult<PromptsResponse> {
        val endpoint = if (forceRefresh) "prompts.json?t=${System.currentTimeMillis()}" else "prompts.json"
        return executeWithRetry("$baseUrl$endpoint", etag) { rawJson ->
            json.decodeFromString<PromptsResponse>(rawJson)
        }
    }

    suspend fun fetchAppConfig(etag: String? = null, forceRefresh: Boolean = false): FetchResult<AppConfig> {
        val endpoint = if (forceRefresh) "app_config.json?t=${System.currentTimeMillis()}" else "app_config.json"
        return executeWithRetry("$baseUrl$endpoint", etag) { rawJson ->
            json.decodeFromString<AppConfig>(rawJson)
        }
    }

    private suspend fun <T> executeWithRetry(
        url: String,
        etag: String?,
        parser: (String) -> T
    ): FetchResult<T> = withContext(Dispatchers.IO) {
        var lastException: Throwable? = null
        var delayMillis = 500L

        for (attempt in 1..3) {
            try {
                val requestBuilder = Request.Builder().url(url)
                if (!etag.isNullOrBlank()) {
                    requestBuilder.addHeader("If-None-Match", etag)
                }

                val response = client.newCall(requestBuilder.build()).execute()
                response.use { resp ->
                    if (resp.code == 304) {
                        return@withContext FetchResult.NotModified
                    }

                    if (!resp.isSuccessful) {
                        throw IOException("HTTP error: ${resp.code} ${resp.message}")
                    }

                    val body = resp.body?.string() ?: throw IOException("Empty response body")
                    val newEtag = resp.header("ETag")
                    val parsed = parser(body)
                    return@withContext FetchResult.Success(parsed, body, newEtag)
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt < 3) {
                    delay(delayMillis)
                    delayMillis *= 2
                }
            }
        }

        FetchResult.Error(
            exception = lastException ?: IOException("Unknown network error"),
            message = lastException?.localizedMessage ?: "Failed to connect after 3 attempts"
        )
    }
}
