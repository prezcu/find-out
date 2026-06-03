package dev.andrei.app_backend.dto;

/** How many photos a location has; the client builds indexed /photo URLs from the count. */
public record LocationPhotosDto(int count) {}
