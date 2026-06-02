package dev.andrei.app_frontend.data.repository

import dev.andrei.app_frontend.data.remote.dto.SubmitReviewRequestDto

interface ReviewRepository {

    /**
     * Submits a review for a location. This is a one-shot request/response (not a stream), so it
     * returns a [Result]: [Result.success] on a 2xx response, or [Result.failure] carrying a
     * user-facing message on any error.
     */
    suspend fun submitReview(request: SubmitReviewRequestDto): Result<Unit>
}