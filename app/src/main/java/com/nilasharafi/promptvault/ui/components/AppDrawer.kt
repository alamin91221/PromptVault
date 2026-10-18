package com.nilasharafi.promptvault.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilasharafi.promptvault.R
import com.nilasharafi.promptvault.ui.theme.BrandCyan
import com.nilasharafi.promptvault.ui.theme.BrandMagenta
import com.nilasharafi.promptvault.ui.theme.BrandPurple
import com.nilasharafi.promptvault.ui.theme.DarkSurfaceElevated

@Composable
fun AppDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onRateApp: () -> Unit,
    onShareApp: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onPremiumPrompts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color(0xFF0F0F1B))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        // App header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_promptvault_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Column {
                Text(
                    text = "PromptVault",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AI Art & Prompts",
                    color = Color(0xFFA5A5BA),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Main nav items
        DrawerNavItem(
            icon = Icons.Rounded.Home,
            label = "Home",
            isSelected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        DrawerNavItem(
            icon = Icons.Rounded.Explore,
            label = "Explore",
            isSelected = currentRoute == "explore",
            onClick = { onNavigate("explore") }
        )
        DrawerNavItem(
            icon = Icons.Rounded.Category,
            label = "Categories",
            isSelected = currentRoute == "categories",
            onClick = { onNavigate("categories") }
        )
        DrawerNavItem(
            icon = Icons.Rounded.Favorite,
            label = "Favorites",
            isSelected = currentRoute == "favorites",
            onClick = { onNavigate("favorites") }
        )
        DrawerNavItem(
            icon = Icons.Rounded.History,
            label = "History",
            isSelected = currentRoute == "history",
            onClick = { onNavigate("history") }
        )
        DrawerNavItem(
            icon = Icons.Rounded.Settings,
            label = "Settings",
            isSelected = currentRoute == "settings",
            onClick = { onNavigate("settings") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Premium promo card inside drawer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF28183E), Color(0xFF1B2040))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(BrandPurple.copy(alpha = 0.5f), BrandCyan.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onPremiumPrompts)
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "👑", fontSize = 24.sp)
                Column {
                    Text(
                        text = "Premium Prompts",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Watch ads to unlock",
                        color = Color(0xFFA5A5BA),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = Color(0xFF25243C))

        Spacer(modifier = Modifier.height(14.dp))

        // Footer links
        DrawerFooterItem(
            icon = Icons.Rounded.Star,
            label = "Rate App",
            onClick = onRateApp
        )
        DrawerFooterItem(
            icon = Icons.Rounded.Share,
            label = "Share App",
            onClick = onShareApp
        )
        DrawerFooterItem(
            icon = Icons.Rounded.Policy,
            label = "Privacy Policy",
            onClick = onPrivacyPolicy
        )
    }
}

@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0x338A2BE2) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) BrandPurple else Color(0xFFA5A5BA),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFFD0D0E2),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun DrawerFooterItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF7A7993),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            color = Color(0xFFA5A5BA),
            fontSize = 13.sp
        )
    }
}
