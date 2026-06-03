package dev.andrei.app_backend.dto.preference;

import java.util.UUID;

/**
 * A concept the user can express a preference for, merged with their current importance
 * (0 when they have never set one). Drives the preference editor.
 */
public record AttributeConceptDto(
        UUID conceptId,
        String slug,
        String displayName,
        String groupLabel,
        int sortOrder,
        int importance
) {}
