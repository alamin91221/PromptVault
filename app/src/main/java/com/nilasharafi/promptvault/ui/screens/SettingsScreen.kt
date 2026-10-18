package com.nilasharafi.promptvault.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilasharafi.promptvault.ads.AdManager
import com.nilasharafi.promptvault.data.repository.PromptRepository
import com.nilasharafi.promptvault.ui.theme.BrandCyan
import com.nilasharafi.promptvault.ui.theme.BrandPurple
import com.nilasharafi.promptvault.ui.theme.DarkBackground
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: PromptRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val currentTheme by repository.themeMode.collectAsState(initial = "dark")
    val currentLang by repository.language.collectAsState(initial = "system")
    val currentRefresh by repository.autoRefresh.collectAsState(initial = "1h")
    val wifiOnly by repository.wifiOnly.collectAsState(initial = false)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Appearance
            SettingsRow(
                icon = Icons.Rounded.DarkMode,
                title = "Appearance",
                subtitle = when (currentTheme) {
                    "dark" -> "Dark Mode"
                    "light" -> "Light Mode"
                    else -> "System Default"
                },
                onClick = {
                    scope.launch {
                        val next = when (currentTheme) {
                            "dark" -> "light"
                            "light" -> "system"
                            else -> "dark"
                        }
                        repository.setThemeMode(next)
                    }
                }
            )

            // Language
            SettingsRow(
                icon = Icons.Rounded.Language,
                title = "Language",
                subtitle = when (currentLang) {
                    "bn" -> "বাংলা (Bangla)"
                    "en" -> "English"
                    else -> "System Default"
                },
                onClick = {
                    scope.launch {
                        val next = if (currentLang == "bn") "en" else "bn"
                        repository.setLanguage(next)
                        Toast.makeText(context, "Language set to $next", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Notifications
            SettingsRow(
                icon = Icons.Rounded.Notifications,
                title = "Notifications",
                subtitle = "On",
                onClick = {}
            )

            // Auto Refresh
            SettingsRow(
                icon = Icons.Rounded.Refresh,
                title = "Auto Refresh",
                subtitle = when (currentRefresh) {
                    "6h" -> "Every 6 hours"
                    "manual" -> "Manual only"
                    else -> "Every 1 hour"
                },
                onClick = {
                    scope.launch {
                        val next = when (currentRefresh) {
                            "1h" -> "6h"
                            "6h" -> "manual"
                            else -> "1h"
                        }
                        repository.setAutoRefresh(next)
                    }
                }
            )

            // Download over Wi-Fi only (switch)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF131322))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = null,
                        tint = Color(0xFFA5A5BA),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Download over Wi-Fi only",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Switch(
                    checked = wifiOnly,
                    onCheckedChange = { checked ->
                        scope.launch { repository.setWifiOnly(checked) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BrandPurple,
                        uncheckedTrackColor = Color(0xFF2C2A47)
                    )
                )
            }

            // Clear Cache
            SettingsRow(
                icon = Icons.Rounded.CleaningServices,
                title = "Clear Cache",
                subtitle = "18.4 MB",
                onClick = {
                    scope.launch {
                        repository.clearCache()
                        Toast.makeText(context, "Cache cleared successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Manage Ad Consent (UMP)
            SettingsRow(
                icon = Icons.Rounded.Security,
                title = "Manage Ad Consent",
                subtitle = "GDPR / Privacy choices",
                onClick = {
                    if (activity != null) {
                        AdManager.instance.showPrivacyOptionsForm(activity)
                    }
                }
            )

            // About
            SettingsRow(
                icon = Icons.Rounded.Info,
                title = "About",
                subtitle = "v1.0.0",
                onClick = {}
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rate App
            SettingsRow(
                icon = Icons.Rounded.Star,
                title = "Rate App",
                subtitle = "Support us on Google Play",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                    }
                }
            )

            // Share App
            SettingsRow(
                icon = Icons.Rounded.Share,
                title = "Share App",
                subtitle = "Share PromptVault with friends",
                onClick = {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out PromptVault for AI Image & Prompts: https://play.google.com/store/apps/details?id=${context.packageName}")
                    }
                    context.startActivity(Intent.createChooser(share, "Share App"))
                }
            )

            // Privacy Policy
            SettingsRow(
                icon = Icons.Rounded.Policy,
                title = "Privacy Policy",
                subtitle = "Read our terms & privacy",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nilasharafi.github.io/PromptVault/privacy.html"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131322))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFA5A5BA),
                modifier = Modifier.size(22.dp)
            )

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF7A7993),
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF5E5C77),
            modifier = Modifier.size(20.dp)
        )
    }
}
