package dev.andrei.app_frontend.data.repository

import dev.andrei.app_frontend.data.remote.dto.MyReviewDto
import dev.andrei.app_frontend.data.remote.dto.ReviewDto
import dev.andrei.app_frontend.data.remote.dto.SubmitReviewRequestDto

interface ReviewRepository {

    /**
     * Submits a review for a location. This is a one-shot request/response (not a stream), so it
     * returns a [Result]: [Result.success] on a 2xx response, or [Result.failure] carrying a
     * user-facing message on any error.
     */
    suspend fun submitReview(request: SubmitReviewRequestDto): Result<Unit>

    /** The reviews shown on a location's public detail page, newest first. */
    suspend fun getLocationReviews(locationId: String): Result<List<ReviewDto>>

    /** The current (signed-in) user's own reviews, newest first. */
    suspend fun getMyReviews(): Result<List<MyReviewDto>>
}