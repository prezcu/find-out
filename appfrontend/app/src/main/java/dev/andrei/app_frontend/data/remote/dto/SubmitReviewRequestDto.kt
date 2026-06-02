package dev.andrei.app_frontend.data.remote.dto

import dev.andrei.app_frontend.ui.state.AttributeRating
import java.util.UUID

data class SubmitReviewRequestDto(
    val locationId: UUID,
    val content: String,
    val attributeRatings: List<AttributeRating>
) {}
