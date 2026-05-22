package dev.andrei.app_backend.dto;

import java.util.UUID;

public record LocationDto(UUID id, String name, String primaryCategory,
                          double longitude, double latitude,boolean hasToilets,
                          boolean hasAccessibilityFeatures, double averageScore) {}
