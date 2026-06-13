package dev.andrei.app_frontend.data.repository

import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.remote.dto.LocationDetailsDto
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface LocationRepository {

    // Rating-sorted nearby list, in server order. Single-shot: a Result so a genuine network
    // failure can surface as an error state (rather than being swallowed into an empty list).
    suspend fun getTopRatedLocations(deviceLongitude: Double, deviceLatitude: Double): Result<List<LocationEntity>>

    // Personalized match-ranked list, in server order (so the match ranking is preserved).
    // Single-shot Result for the same reason as getTopRatedLocations.
    suspend fun getRecommendedLocations(deviceLongitude: Double, deviceLatitude: Double): Result<List<LocationEntity>>

    // "Try something new": nearby high-quality places that lean on concepts the user usually doesn't
    // prioritize, in server (discovery-ranked) order. Empty when the user has no effective preferences.
    suspend fun getDiscoveryLocations(deviceLongitude: Double, deviceLatitude: Double): Result<List<LocationEntity>>

    fun getLocationById(id: UUID): Flow<LocationEntity?>

    suspend fun searchLocationsByName(query: String): Result<List<LocationEntity>>

    /**
     * Absolute, ready-to-load photo URLs for a location's carousel. Empty when the location has no
     * photos (or the backend has no Google key configured). Never throws.
     */
    suspend fun getPhotoUrls(locationId: String): List<String>

    /**
     * The venue's street address + weekly opening hours, fetched on attraction view (the backend
     * resolves them from Google once, then serves the cache). Null when unavailable or on failure;
     * never throws.
     */
    suspend fun getDetails(locationId: String): LocationDetailsDto?
}