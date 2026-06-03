package dev.andrei.app_backend.dto.preference;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Body of PUT /api/preferences. The user's identity is taken from the JWT. */
public record UpdatePreferencesRequest(
        @NotNull @Valid List<PreferenceUpdateDto> preferences
) {}
