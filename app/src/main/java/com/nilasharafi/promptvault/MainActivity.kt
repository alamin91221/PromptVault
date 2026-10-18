package com.nilasharafi.promptvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.nilasharafi.promptvault.ads.AdManager
import com.nilasharafi.promptvault.data.model.AdsConfig
import com.nilasharafi.promptvault.ui.PromptVaultAppScreen
import com.nilasharafi.promptvault.ui.theme.PromptVaultTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = PromptVaultApp.instance.repository

        // UMP GDPR Consent & AdMob initialization
        val adsConfigFlow = MutableStateFlow(repository.configState.value.ads)
        lifecycleScope.launch {
            repository.configState.collect { appConfig ->
                adsConfigFlow.value = appConfig.ads
            }
        }
        AdManager.instance.initializeWithConsent(this, adsConfigFlow)

        setContent {
            val themeSetting by repository.themeMode.collectAsState(initial = "dark")

            PromptVaultTheme(themeSetting = themeSetting) {
                PromptVaultAppScreen(
                    repository = repository,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}