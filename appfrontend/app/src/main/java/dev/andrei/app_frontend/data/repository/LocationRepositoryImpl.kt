package dev.andrei.app_frontend.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationToken
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.andrei.app_frontend.data.local.dao.LocationDao
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.remote.ApiConfig
import dev.andrei.app_frontend.data.remote.api.ApiService
import dev.andrei.app_frontend.data.remote.dto.JustCoordinatesDto
import dev.andrei.app_frontend.data.remote.dto.LocationDto
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject


class LocationRepositoryImpl @Inject constructor(
    private val dao: LocationDao,
    private val api: ApiService,
): LocationRepository {

    override suspend fun getTopRatedLocations(deviceLongitude: Double, deviceLatitude: Double): Result<List<LocationEntity>> =
        fetchLocations { api.fetchTop10CloseLocations(JustCoordinatesDto(deviceLatitude, deviceLongitude)) }

    override suspend fun getRecommendedLocations(deviceLongitude: Double, deviceLatitude: Double): Result<List<LocationEntity>> =
        fetchLocations { api.fetchRecommendedLocations(JustCoordinatesDto(deviceLatitude, deviceLongitude)) }

    // Shared shape for the landing lists: call the endpoint, keep the server order, and upsert into
    // Room (without clearing) so the detail screen can resolve any card by id. The two lists coexist
    // for a signed-in user, so clearing here would wipe the other list's rows. Mirrors searchLocationsByName.
    private suspend fun fetchLocations(
        call: suspend () -> Response<List<LocationDto>>
    ): Result<List<LocationEntity>> = runCatching {
        val response = call()
        if (!response.isSuccessful) {
            error("Request failed: ${response.code()}")
        }
        val entities = response.body().orEmpty().map { it.toEntity() }
        if (entities.isNotEmpty()) {
            dao.insertLocations(entities)
        }
        entities
    }

    override fun getLocationById(id: UUID): Flow<LocationEntity?> = dao.getLocationById(id)

    override suspend fun getPhotoUrls(locationId: String): List<String> {
        return try {
            val response = api.getLocationPhotoCount(locationId)
            val count = response.body()?.count ?: 0
            if (!response.isSuccessful || count <= 0) {
                emptyList()
            } else {
                // The backend serves each photo at /photo?index=N; build one URL per index.
                List(count) { index -> ApiConfig.photoUrl(locationId, index) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun searchLocationsByName(query: String): Result<List<LocationEntity>> {
        return runCatching {
            val response = api.searchLocationsByName(query)
            if (!response.isSuccessful) {
                error("Search failed: ${response.code()}")
            }
            val entities = response.body().orEmpty().map { it.toEntity() }
            if (entities.isNotEmpty()) {
                dao.insertLocations(entities)
            }
            entities
        }
    }
}
