package dev.andrei.app_frontend.data.remote.dto

// createdAt is an ISO-8601 string (the backend serialises Instant that way).
data class ReviewDto(
    val id: String,
    val reviewerDisplayName: String,
    val content: String,
    val createdAt: String,
    val overallScore: Float,
    val attributeScores: List<AttributeScoreDto>
)
