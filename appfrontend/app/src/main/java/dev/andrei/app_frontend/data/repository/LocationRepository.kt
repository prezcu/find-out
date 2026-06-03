package dev.andrei.app_frontend.data.repository

import dev.andrei.app_frontend.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface LocationRepository {

    fun getTop10CloseLocations(deviceLongitude: Double, deviceLatitude: Double): Flow<List<LocationEntity>>

    // Personalized match-ranked list. Emits the server order directly (no Room cache,
    // which would re-sort by averageScore and lose the match ranking).
    fun getRecommendedLocations(deviceLongitude: Double, deviceLatitude: Double): Flow<List<LocationEntity>>

    fun getLocationById(id: UUID): Flow<LocationEntity?>

    suspend fun searchLocationsByName(query: String): Result<List<LocationEntity>>
}