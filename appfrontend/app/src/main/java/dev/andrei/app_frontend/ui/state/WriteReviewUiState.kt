package dev.andrei.app_frontend.ui.state

data class WriteReviewUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
