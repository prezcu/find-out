package dev.andrei.app_backend.dto;

import java.util.List;

/**
 * On-demand details for a location's attraction screen: its street address and weekly opening hours,
 * resolved from Google Place Details on first view and cached. {@code address} is null when
 * unavailable; {@code openingHours} is empty when the venue has no hours.
 */
public record LocationDetailsDto(String address, List<OpeningHoursDto> openingHours) {}
