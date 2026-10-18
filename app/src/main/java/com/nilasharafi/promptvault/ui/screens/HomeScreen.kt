package com.nilasharafi.promptvault.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilasharafi.promptvault.ads.AdManager
import com.nilasharafi.promptvault.ads.BannerAdView
import com.nilasharafi.promptvault.data.model.PromptItem
import com.nilasharafi.promptvault.data.model.UnlockState
import com.nilasharafi.promptvault.data.repository.PromptRepository
import com.nilasharafi.promptvault.ui.components.CategoryChipsRow
import com.nilasharafi.promptvault.ui.components.NativeAdCard
import com.nilasharafi.promptvault.ui.components.PromptCard
import com.nilasharafi.promptvault.ui.components.TopNavBar
import com.nilasharafi.promptvault.ui.theme.DarkBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: PromptRepository,
    onPromptClick: (PromptItem) -> Unit,
    onSearchClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val promptsResponse by repository.promptsState.collectAsState()
    val config by repository.configState.collectAsState()
    val isRefreshing by repository.isRefreshing.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    var selectedCategory by remember { mutableStateOf("All") }

    val filteredItems = remember(promptsResponse, selectedCategory) {
        if (selectedCategory.equals("All", ignoreCase = true)) {
            promptsResponse.items
        } else {
            promptsResponse.items.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        // Top Bar
        TopNavBar(
            onSearchClick = onSearchClick,
            onPremiumClick = onPremiumClick,
            onMenuClick = onMenuClick
        )

        // Category Chips Row
        CategoryChipsRow(
            categories = promptsResponse.categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Main Feed with Pull to refresh
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    repository.refreshAll(force = true)
                }
            },
            state = pullToRefreshState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (filteredItems.isEmpty() && !isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No prompts found for $selectedCategory",
                        color = Color(0xFFA5A5BA),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = filteredItems,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        val unlockState by repository.isPromptUnlocked(item.id, item.isPremium)
                            .collectAsState(initial = UnlockState.Locked)

                        PromptCard(
                            item = item,
                            unlockState = unlockState,
                            isNew = index == 0 || index == 1,
                            onClick = {
                                if (activity != null) {
                                    AdManager.instance.onPromptViewed(activity, config.ads)
                                }
                                onPromptClick(item)
                            }
                        )

                        // Insert Native Ad Card per remote config rule (start_after_items, every_n_items)
                        val nativeConfig = config.ads.native
                        if (config.ads.masterEnabled && nativeConfig.enabled) {
                            val shouldInsertAd = index >= nativeConfig.startAfterItems &&
                                    (index - nativeConfig.startAfterItems) % nativeConfig.everyNItems == 0

                            if (shouldInsertAd) {
                                NativeAdCard(
                                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom anchored banner ad (if configured for "home")
        BannerAdView(
            adsConfig = config.ads,
            screenName = "home"
        )
    }
}
