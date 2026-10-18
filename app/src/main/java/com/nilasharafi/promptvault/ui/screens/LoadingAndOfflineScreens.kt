package com.nilasharafi.promptvault.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilasharafi.promptvault.ui.theme.BrandCyan
import com.nilasharafi.promptvault.ui.theme.BrandPurple
import com.nilasharafi.promptvault.ui.theme.DarkBackground
import com.nilasharafi.promptvault.ui.theme.PrimaryGradient
import kotlinx.coroutines.delay

@Composable
fun LoadingContentScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0.1f) }
    var currentStep by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        progress = 0.3f
        delay(400)
        currentStep = 2
        progress = 0.65f
        delay(400)
        currentStep = 3
        progress = 0.9f
        delay(350)
        currentStep = 4
        progress = 1.0f
        delay(300)
        onFinished()
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 350),
        label = "progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Glowing Cloud Download Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0x3300E5FF), Color(0x0500E5FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudDownload,
                    contentDescription = "Loading",
                    tint = BrandCyan,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Loading Content...",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fetching latest prompts and ads\nconfiguration from GitHub",
                color = Color(0xFFA5A5BA),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                color = BrandCyan,
                trackColor = Color(0xFF1B1B30),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4-Step Checklist
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LoadingCheckRow(text = "Checking connection", isCompleted = currentStep >= 1)
                LoadingCheckRow(text = "Fetching prompts.json", isCompleted = currentStep >= 2)
                LoadingCheckRow(text = "Fetching ads_config.json", isCompleted = currentStep >= 3)
                LoadingCheckRow(text = "Updating local cache", isCompleted = currentStep >= 4)
            }
        }
    }
}

@Composable
private fun LoadingCheckRow(
    text: String,
    isCompleted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = if (isCompleted) Color(0xFF00E676) else Color(0xFF3E3D58),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            color = if (isCompleted) Color(0xFFE2E2F0) else Color(0xFF7A7993),
            fontSize = 14.sp,
            fontWeight = if (isCompleted) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun OfflineScreen(
    onTryAgain: () -> Unit,
    onViewCached: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Red Wi-Fi disconnected icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0x33FF3D71), Color(0x05FF3D71))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.WifiOff,
                    contentDescription = "Offline",
                    tint = Color(0xFFFF3D71),
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "You're Offline",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Using cached content. Some features may be limited.",
                color = Color(0xFFA5A5BA),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // "Try Again" gradient button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(PrimaryGradient)
                    .clickable(onClick = onTryAgain),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Try Again",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "View Cached Content" text link
            Text(
                text = "View Cached Content",
                color = Color(0xFFA5A5BA),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(onClick = onViewCached)
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            )
        }
    }
}
