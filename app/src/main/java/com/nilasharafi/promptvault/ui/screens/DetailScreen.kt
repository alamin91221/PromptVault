package com.nilasharafi.promptvault.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.nilasharafi.promptvault.ads.AdManager
import com.nilasharafi.promptvault.ads.BannerAdView
import com.nilasharafi.promptvault.data.model.PromptItem
import com.nilasharafi.promptvault.data.model.UnlockState
import com.nilasharafi.promptvault.data.repository.PromptRepository
import com.nilasharafi.promptvault.ui.components.PremiumPromptDialog
import com.nilasharafi.promptvault.ui.components.PromptPopupDialog
import com.nilasharafi.promptvault.ui.theme.BrandCyan
import com.nilasharafi.promptvault.ui.theme.BrandMagenta
import com.nilasharafi.promptvault.ui.theme.BrandPurple
import com.nilasharafi.promptvault.ui.theme.PrimaryGradient
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    itemId: String,
    repository: PromptRepository,
    onBack: () -> Unit,
    onRelatedItemClick: (PromptItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val promptsResponse by repository.promptsState.collectAsState()
    val config by repository.configState.collectAsState()
    val item = remember(promptsResponse, itemId) {
        promptsResponse.items.find { it.id == itemId } ?: promptsResponse.items.firstOrNull()
    } ?: return

    val unlockState by repository.isPromptUnlocked(item.id, item.isPremium)
        .collectAsState(initial = UnlockState.Locked)

    val favorites by repository.favoriteIds.collectAsState(initial = emptySet())
    val isFavorite = favorites.contains(item.id)

    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var showPromptPopup by remember { mutableStateOf(false) }
    var showPremiumPopup by remember { mutableStateOf(false) }
    var isLoadingAd by remember { mutableStateOf(false) }
    var adErrorMessage by remember { mutableStateOf<String?>(null) }

    // Pinch-to-zoom state
    var scale by remember { mutableFloatStateOf(1f) }
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
    }

    val relatedItems = remember(promptsResponse, item) {
        promptsResponse.items.filter { it.category == item.category && it.id != item.id }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090912))
    ) {
        // Full-bleed Image with pinch-to-zoom
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
        )

        // Gradient vignettes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xD9090912), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xCC090912),
                            Color(0xF2090912),
                            Color(0xFF090912)
                        )
                    )
                )
        )

        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button (glass circle)
            GlassCircleButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )

            // Right actions: Favorite & Share
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCircleButton(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFFF4081) else Color.White,
                    onClick = {
                        scope.launch {
                            repository.toggleFavorite(item.id)
                        }
                    }
                )

                GlassCircleButton(
                    icon = Icons.Filled.Share,
                    contentDescription = "Share",
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, item.title)
                            putExtra(Intent.EXTRA_TEXT, "${item.title}\n${item.imageUrl}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Prompt"))
                    }
                )
            }
        }

        // Bottom Content Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Title + Crown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                if (item.isPremium) {
                    Text(text = "👑", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaChip(text = "👤 ${item.category}")
                if (item.model.isNotBlank()) {
                    MetaChip(text = item.model)
                }
                MetaChip(text = "📅 Oct 1, 2026")
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Signature "🔒 View Prompt" button (gradient pill)
            val isLocked = item.isPremium && unlockState !is UnlockState.Unlocked

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(PrimaryGradient)
                    .clickable {
                        if (isLocked) {
                            showPremiumPopup = true
                        } else {
                            showPromptPopup = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.AutoAwesome,
                        contentDescription = "View Prompt",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isLocked) "View Prompt (Locked)" else "View Prompt",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // "More like this" horizontal list
            if (relatedItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "More like this",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = ">>",
                        color = Color(0xFFA5A5BA),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(relatedItems) { relItem ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(relItem.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = relItem.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 86.dp, height = 90.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onRelatedItemClick(relItem) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Banner ad at the bottom of detail screen
            BannerAdView(
                adsConfig = config.ads,
                screenName = "detail"
            )
        }

        // Unlocked Prompt Popup Dialog
        if (showPromptPopup) {
            val secureText = remember(item, unlockState) {
                if (!item.isPremium || unlockState is UnlockState.Unlocked) {
                    item.prompt
                } else {
                    "Locked"
                }
            }

            PromptPopupDialog(
                item = item,
                promptText = secureText,
                onDismiss = { showPromptPopup = false }
            )
        }

        // Locked Premium Prompt Dialog
        if (showPremiumPopup) {
            PremiumPromptDialog(
                unlockDurationHours = config.premium.unlockDurationHours,
                isLoadingAd = isLoadingAd,
                errorMessage = adErrorMessage,
                onWatchAdClick = {
                    if (activity != null) {
                        isLoadingAd = true
                        adErrorMessage = null
                        AdManager.instance.showRewardedAd(
                            activity = activity,
                            config = config.ads,
                            onRewardEarned = {
                                isLoadingAd = false
                                scope.launch {
                                    repository.unlockPrompt(item.id)
                                    showPremiumPopup = false
                                    showPromptPopup = true
                                }
                            },
                            onAdFailed = { error ->
                                isLoadingAd = false
                                adErrorMessage = error
                            }
                        )
                    }
                },
                onDismiss = {
                    showPremiumPopup = false
                    adErrorMessage = null
                }
            )
        }
    }
}

@Composable
private fun GlassCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0x66000000))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MetaChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33FFFFFF))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFFE2E2F2),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
