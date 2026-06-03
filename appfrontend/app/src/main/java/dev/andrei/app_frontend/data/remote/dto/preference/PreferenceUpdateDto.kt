package dev.andrei.app_frontend.data.remote.dto.preference

/** One concept's importance (0–5) in an update request. */
data class PreferenceUpdateDto(
    val conceptId: String,
    val importance: Int
)
