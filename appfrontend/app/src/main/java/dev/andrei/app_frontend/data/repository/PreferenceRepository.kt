package dev.andrei.app_frontend.data.repository

import dev.andrei.app_frontend.data.remote.dto.preference.AttributeConceptDto
import dev.andrei.app_frontend.data.remote.dto.preference.PreferenceUpdateDto

interface PreferenceRepository {

    suspend fun getPreferences(): Result<List<AttributeConceptDto>>

    suspend fun updatePreferences(prefs: List<PreferenceUpdateDto>): Result<Unit>
}
