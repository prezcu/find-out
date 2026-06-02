package dev.andrei.app_backend.dto.review;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Body of POST /api/reviews. The reviewer's identity is NOT here — it is taken from the JWT.
 * Field names match the Android client's SubmitReviewRequestDto exactly.
 */
public record SubmitReviewRequest(
        @NotNull UUID locationId,
        @Size(max = 2000) String content,
        @NotEmpty @Valid List<AttributeRatingDto> attributeRatings
) {}
