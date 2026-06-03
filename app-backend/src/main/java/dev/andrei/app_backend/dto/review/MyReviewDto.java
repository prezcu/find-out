package dev.andrei.app_backend.dto.review;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A review as shown on the author's own "My reviews" list (carries the location it belongs to). */
public record MyReviewDto(
        UUID id,
        UUID locationId,
        String locationName,
        String content,
        Instant createdAt,
        double overallScore,
        List<AttributeScoreDto> attributeScores
) {}