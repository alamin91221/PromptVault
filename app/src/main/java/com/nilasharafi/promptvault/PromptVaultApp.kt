package com.nilasharafi.promptvault

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.nilasharafi.promptvault.data.local.PromptDataStore
import com.nilasharafi.promptvault.data.remote.PromptApiService
import com.nilasharafi.promptvault.data.repository.PromptRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class PromptVaultApp : Application(), SingletonImageLoader.Factory {

    lateinit var dataStore: PromptDataStore
        private set

    lateinit var apiService: PromptApiService
        private set

    lateinit var repository: PromptRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        dataStore = PromptDataStore(this)
        apiService = PromptApiService()
        repository = PromptRepository(apiService, dataStore)
    }

    override fun newImageLoader(context: android.content.Context): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(okHttpClient))
            }
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var instance: PromptVaultApp
            private set
    }
}
