package com.nilasharafi.promptvault.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.nilasharafi.promptvault.data.model.PromptItem
import com.nilasharafi.promptvault.ui.theme.BrandCyan
import com.nilasharafi.promptvault.ui.theme.BrandMagenta
import com.nilasharafi.promptvault.ui.theme.BrandPurple
import com.nilasharafi.promptvault.ui.theme.DarkBorder
import com.nilasharafi.promptvault.ui.theme.DarkSurface
import com.nilasharafi.promptvault.ui.theme.DarkSurfaceVariant
import com.nilasharafi.promptvault.ui.theme.PrimaryGradient
import kotlinx.coroutines.delay

@Composable
fun PromptPopupDialog(
    item: PromptItem,
    promptText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copiedPrompt by remember { mutableStateOf(false) }
    var copiedNegative by remember { mutableStateOf(false) }
    var copiedModel by remember { mutableStateOf(false) }

    // Typewriter effect state
    var displayedPrompt by remember { mutableStateOf("") }
    LaunchedEffect(promptText) {
        displayedPrompt = ""
        val step = 4
        for (i in step..promptText.length step step) {
            displayedPrompt = promptText.substring(0, i)
            delay(10)
        }
        displayedPrompt = promptText
    }

    fun copyToClipboard(label: String, text: String, onDone: () -> Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        onDone()
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFF131322))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF3B395E), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .clickable(enabled = false) {}
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF484668))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header with thumbnail & Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Column {
                        Text(
                            text = "Prompt",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.title,
                            color = Color(0xFFA5A5BA),
                            fontSize = 12.sp
                        )
                    }
                }

                // Close button "✕"
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // PROMPT SECTION
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prompt",
                            color = Color(0xFFA5A5BA),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        CopyButton(
                            isCopied = copiedPrompt,
                            onClick = {
                                copyToClipboard("Prompt", promptText) {
                                    copiedPrompt = true
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1B1B2F))
                            .border(1.dp, Color(0xFF2C2A47), RoundedCornerShape(14.dp))
                            .clickable { displayedPrompt = promptText }
                            .padding(14.dp)
                    ) {
                        Text(
                            text = displayedPrompt,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            fontFamily = FontFamily.Default
                        )
                    }
                }

                // NEGATIVE PROMPT SECTION
                if (item.negativePrompt.isNotBlank()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Negative Prompt",
                                color = Color(0xFFA5A5BA),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            CopyButton(
                                isCopied = copiedNegative,
                                onClick = {
                                    copyToClipboard("Negative Prompt", item.negativePrompt) {
                                        copiedNegative = true
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1B1B2F))
                                .border(1.dp, Color(0xFF2C2A47), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = item.negativePrompt,
                                color = Color(0xFFD6D6E8),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // MODEL SECTION
                if (item.model.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1B1B2F))
                            .border(1.dp, Color(0xFF2C2A47), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Model: ",
                                color = Color(0xFFA5A5BA),
                                fontSize = 13.sp
                            )
                            Text(
                                text = item.model,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = {
                                copyToClipboard("Model", item.model) {
                                    copiedModel = true
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (copiedModel) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                contentDescription = "Copy Model",
                                tint = if (copiedModel) Color(0xFF00E676) else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Close button (gradient pill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(PrimaryGradient)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Close",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CopyButton(
    isCopied: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x338A2BE2))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
            contentDescription = "Copy",
            tint = if (isCopied) Color(0xFF00E676) else Color.White,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = if (isCopied) "Copied!" else "Copy",
            color = if (isCopied) Color(0xFF00E676) else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
