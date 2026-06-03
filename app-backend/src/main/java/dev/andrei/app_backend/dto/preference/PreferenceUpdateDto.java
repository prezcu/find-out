package dev.andrei.app_backend.dto.preference;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** One concept's importance in an update request. */
public record PreferenceUpdateDto(
        @NotNull UUID conceptId,
        @NotNull @Min(0) @Max(5) Integer importance
) {}
