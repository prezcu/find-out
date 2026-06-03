package dev.andrei.app_frontend.data.remote.dto.preference

/** Body of PUT /api/preferences. */
data class UpdatePreferencesRequestDto(
    val preferences: List<PreferenceUpdateDto>
)
