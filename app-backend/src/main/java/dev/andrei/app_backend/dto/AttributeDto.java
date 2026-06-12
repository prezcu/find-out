package dev.andrei.app_backend.dto;

/**
 * An attribute as exposed to the client: {@code name} is the stable key used by the review API;
 * {@code conceptSlug} is the slug of the mapped {@link dev.andrei.app_backend.model.AttributeConcept}
 * (nullable — null when the attribute maps to no concept), used by the client to join the user's
 * per-concept weight; {@code averageScore} is this location attribute's running average score.
 */
public record AttributeDto(String name, String conceptSlug, double averageScore) {}
