package com.nilasharafi.promptvault.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromptsResponse(
    @SerialName("schema_version")
    val schemaVersion: Int = 1,
    @SerialName("updated_at")
    val updatedAt: String = "",
    val categories: List<String> = emptyList(),
    val items: List<PromptItem> = emptyList()
)

@Serializable
data class PromptItem(
    val id: String = "",
    val title: String = "",
    @SerialName("image_url")
    val imageUrl: String = "",
    val prompt: String = "",
    @SerialName("negative_prompt")
    val negativePrompt: String = "",
    val model: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(),
    @SerialName("is_premium")
    val isPremium: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = ""
)

sealed interface UnlockState {
    data object Free : UnlockState
    data class Unlocked(val remainingFormatted: String, val expiresAtMillis: Long) : UnlockState
    data object Locked : UnlockState
}
