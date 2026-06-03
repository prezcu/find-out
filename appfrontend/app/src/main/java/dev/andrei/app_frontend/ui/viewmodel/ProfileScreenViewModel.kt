package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.remote.dto.MyReviewDto
import dev.andrei.app_frontend.data.repository.AuthRepository
import dev.andrei.app_frontend.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _logInState = MutableStateFlow(false)
    val logInState = _logInState.asStateFlow()

    private val _myReviews = MutableStateFlow<List<MyReviewDto>>(emptyList())
    val myReviews = _myReviews.asStateFlow()

    private val _reviewsLoading = MutableStateFlow(false)
    val reviewsLoading = _reviewsLoading.asStateFlow()

    /** Refreshes the login flag and, when signed in, the user's own reviews. */
    fun updateLogInState() {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn()
            _logInState.value = loggedIn
            if (!loggedIn) {
                _myReviews.value = emptyList()
                return@launch
            }
            _reviewsLoading.value = true
            reviewRepository.getMyReviews().onSuccess { _myReviews.value = it }
            _reviewsLoading.value = false
        }
    }
}
