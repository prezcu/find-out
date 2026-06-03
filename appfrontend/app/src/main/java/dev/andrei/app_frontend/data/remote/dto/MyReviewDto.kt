package dev.andrei.app_frontend.data.remote.dto

// A review by the current user, carrying the location it belongs to.
data class MyReviewDto(
    val id: String,
    val locationId: String,
    val locationName: String,
    val content: String,
    val createdAt: String,
    val overallScore: Float,
    val attributeScores: List<AttributeScoreDto>
)