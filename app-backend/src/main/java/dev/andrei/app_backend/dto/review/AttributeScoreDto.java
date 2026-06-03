package dev.andrei.app_backend.dto.review;

/** One attribute's score as stored on a review, e.g. "Cleanliness" -> 4.5. */
public record AttributeScoreDto(String attribute, double score) {}
