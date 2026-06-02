package dev.andrei.app_backend.dto.review;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** One attribute the user rated. The 0.5-step rule is enforced in the service. */
public record AttributeRatingDto(
        @NotBlank String attribute,
        @NotNull @DecimalMin("0.5") @DecimalMax("5.0") Double rating
) {}