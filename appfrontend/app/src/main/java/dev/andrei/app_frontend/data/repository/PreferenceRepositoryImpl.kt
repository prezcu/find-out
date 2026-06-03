package dev.andrei.app_frontend.data.repository

import dev.andrei.app_frontend.data.remote.api.ApiService
import dev.andrei.app_frontend.data.remote.dto.preference.AttributeConceptDto
import dev.andrei.app_frontend.data.remote.dto.preference.PreferenceUpdateDto
import dev.andrei.app_frontend.data.remote.dto.preference.UpdatePreferencesRequestDto
import retrofit2.Response
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(
    private val api: ApiService
) : PreferenceRepository {

    override suspend fun getPreferences(): Result<List<AttributeConceptDto>> = runCatching {
        val response = api.getPreferences()
        if (!response.isSuccessful) error(response.toUserMessage())
        response.body().orEmpty()
    }

    override suspend fun updatePreferences(prefs: List<PreferenceUpdateDto>): Result<Unit> = runCatching {
        val response = api.updatePreferences(UpdatePreferencesRequestDto(prefs))
        if (!response.isSuccessful) error(response.toUserMessage())
        Unit
    }

    private fun Response<*>.toUserMessage(): String = when (code()) {
        401 -> "You need to be signed in"
        in 400..499 -> "Request failed (${code()})"
        in 500..599 -> "Server error, please try again later"
        else -> "Unexpected error (${code()})"
    }
}
