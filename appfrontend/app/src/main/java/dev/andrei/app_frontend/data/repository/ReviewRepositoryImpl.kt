package dev.andrei.app_frontend.data.repository

import dev.andrei.app_frontend.data.remote.api.ApiService
import dev.andrei.app_frontend.data.remote.dto.SubmitReviewRequestDto
import retrofit2.Response
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val api: ApiService
) : ReviewRepository {

    override suspend fun submitReview(request: SubmitReviewRequestDto): Result<Unit> =
        runCatching {
            val response = api.submitReview(request)
            if (!response.isSuccessful) {
                // turns into Result.failure
                // the message is surfaced to the user by the viewmodel
                error(response.toUserMessage())
            }
            Unit
        }

    private fun Response<*>.toUserMessage(): String = when (code()) {
        401 -> "You need to be signed in to post a review"
        409 -> "You have already reviewed this location"
        in 400..499 -> "Your review could not be submitted (${code()})"
        in 500..599 -> "Server error, please try again later"
        else -> "Unexpected error (${code()})"
    }
}