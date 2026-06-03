package dev.andrei.app_frontend.data.remote.dto

data class AttributeScoreDto(
    val attribute: String,
    val score: Float,
    val displayName: String? = null
)