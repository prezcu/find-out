package dev.andrei.app_backend.dto;

/**
 * One opening interval for a location's weekly schedule. {@code dayOfWeek} is ISO 1=Mon..7=Sun;
 * times are {@code HH:mm}. {@code 00:00–00:00} means open 24 hours; a close earlier than open
 * crosses midnight. The client renders these and derives a live open-now status from them.
 */
public record OpeningHoursDto(short dayOfWeek, String openTime, String closeTime, boolean isClosed) {}
