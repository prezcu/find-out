package dev.andrei.app_backend.dto;

/**
 * An attribute as exposed to the client: {@code name} is the stable key used by the review
 * API; {@code displayName} is the UI-friendly label (nullable — client prettifies the name
 * as a fallback).
 */
public record AttributeDto(String name) {}
