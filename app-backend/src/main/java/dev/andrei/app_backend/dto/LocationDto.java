package dev.andrei.app_backend.dto;

import java.util.List;
import java.util.UUID;

public record LocationDto(UUID id, String name, String primaryCategory,
                          // UI-friendly category label; null -> client prettifies primaryCategory.
                          String primaryCategoryDisplayName,
                          double longitude, double latitude,boolean hasToilets,
                          boolean hasAccessibilityFeatures, double averageScore, List<AttributeDto> attributes,
                          // Per-user match score (0–5) when ranked by preferences; null otherwise.
                          Double matchScore) {}
