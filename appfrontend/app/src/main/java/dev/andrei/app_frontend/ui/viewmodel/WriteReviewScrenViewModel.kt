package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.remote.dto.SubmitReviewRequestDto
import dev.andrei.app_frontend.data.repository.LocationRepository
import dev.andrei.app_frontend.data.repository.ReviewRepository
import dev.andrei.app_frontend.ui.navigation.WriteReviewRoute
import dev.andrei.app_frontend.ui.state.ReviewDraft
import dev.andrei.app_frontend.ui.state.WriteReviewUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class WriteReviewScrenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    locationRepository: LocationRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val args = savedStateHandle.toRoute<WriteReviewRoute>()
    private val locationId = UUID.fromString(args.locationId)

    val location: StateFlow<LocationEntity?> = locationRepository
        .getLocationById(locationId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _uiState = MutableStateFlow(WriteReviewUiState())
    val uiState = _uiState.asStateFlow()


    fun submitReview(draft: ReviewDraft) {
        // only the attributes the user actually rated count (rating > 0 == rated).
        val ratedAttributes = draft.attributeRatings.filter { it.rating > 0f }
        if (ratedAttributes.size < MIN_RATED_ATTRIBUTES) {
            _uiState.update {
                it.copy(errorMessage = "Please rate at least $MIN_RATED_ATTRIBUTES attributes")
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val request = SubmitReviewRequestDto(
                locationId = locationId,
                content = draft.reviewText.trim(),
                attributeRatings = ratedAttributes
            )
            reviewRepository.submitReview(request).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = e.message ?: "Something went wrong")
                    }
                }
            )
        }
    }

    private companion object {
        const val MIN_RATED_ATTRIBUTES = 3
    }
}