package dev.andrei.app_frontend.ui.state

data class AttributeRating(
    val attribute: String,
    val rating: Float
)

/**
 * Everything the user entered on the WriteReviewScreen
 * The location id is intentionally not here: the viewmodel already knows it from navigation arguments
 */
data class ReviewDraft(
    val attributeRatings: List<AttributeRating>,
    val reviewText: String
)
