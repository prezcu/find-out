package dev.andrei.app_backend.dto.review;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A review as shown on a location's public review list. overallScore = mean of attribute scores. */
public record ReviewDto(
        UUID id,
        String reviewerDisplayName,
        String content,
        Instant createdAt,
        double overallScore,
        List<AttributeScoreDto> attributeScores
) {}