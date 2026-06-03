package dev.andrei.app_backend.dto.review;

/**
 * One attribute's score as stored on a review, e.g. "Cleanliness" -> 4.5. {@code attribute}
 * is the stable name; {@code displayName} is the UI-friendly label (nullable).
 */
public record AttributeScoreDto(String attribute, double score) {}
