package dev.andrei.app_frontend.data.remote.dto.preference

/** A concept the user can rate, with their current importance (0 when never set). */
data class AttributeConceptDto(
    val conceptId: String,
    val slug: String,
    val displayName: String,
    val groupLabel: String?,
    val sortOrder: Int,
    val importance: Int
)
