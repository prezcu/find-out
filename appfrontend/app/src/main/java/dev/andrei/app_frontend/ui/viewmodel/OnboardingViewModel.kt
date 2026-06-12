package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.remote.dto.preference.AttributeConceptDto
import dev.andrei.app_frontend.data.remote.dto.preference.PreferenceUpdateDto
import dev.andrei.app_frontend.data.repository.PreferenceRepository
import dev.andrei.app_frontend.ui.util.ONBOARDING_SLUGS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the post-registration onboarding wizard. Loads the full concept catalogue, keeps only the
 * curated [ONBOARDING_SLUGS] (in that order), and persists them with a single partial update — the
 * backend upserts just the concepts we send and leaves the rest at their default.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _concepts = MutableStateFlow<List<AttributeConceptDto>>(emptyList())
    val concepts = _concepts.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _step = MutableStateFlow(0)
    val step = _step.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            preferenceRepository.getPreferences()
                .onSuccess { all ->
                    val bySlug = all.associateBy { it.slug }
                    _concepts.value = ONBOARDING_SLUGS.mapNotNull { bySlug[it] }
                }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    /** Advance / retreat through the curated attributes; [last] is the final (confirmation) index. */
    fun next(last: Int) { _step.value = (_step.value + 1).coerceAtMost(last) }
    fun back() { _step.value = (_step.value - 1).coerceAtLeast(0) }

    /** Local-only edit; persisted on [finish]. */
    fun setImportance(conceptId: String, importance: Int) {
        _concepts.value = _concepts.value.map {
            if (it.conceptId == conceptId) it.copy(importance = importance) else it
        }
    }

    fun finish() {
        viewModelScope.launch {
            _saving.value = true
            _error.value = null
            val updates = _concepts.value.map { PreferenceUpdateDto(it.conceptId, it.importance) }
            preferenceRepository.updatePreferences(updates)
                .onSuccess { _saved.value = true }
                .onFailure { _error.value = it.message }
            _saving.value = false
        }
    }
}
