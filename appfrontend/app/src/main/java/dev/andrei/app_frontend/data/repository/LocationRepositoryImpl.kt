package dev.andrei.app_frontend.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationToken
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.andrei.app_frontend.data.local.dao.LocationDao
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.remote.api.ApiService
import dev.andrei.app_frontend.data.remote.dto.JustCoordinatesDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import java.util.UUID
import javax.inject.Inject


class LocationRepositoryImpl @Inject constructor(
    private val dao: LocationDao,
    private val api: ApiService,
): LocationRepository {

    override fun getTop10CloseLocations(deviceLongitude: Double, deviceLatitude: Double): Flow<List<LocationEntity>> {
        return dao.getLocationsOrderedByName()
            .onStart {
                refreshLocations(deviceLongitude, deviceLatitude)
            }
            .catch {
                e -> emit(emptyList())
            }
    }

    override fun getLocationById(id: UUID): Flow<LocationEntity?> = dao.getLocationById(id)

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

    private suspend fun  refreshLocations(deviceLongitude: Double, deviceLatitude: Double) {
        try {
            dao.clearLocations()

            val request = JustCoordinatesDto(deviceLatitude, deviceLongitude)
            val response = api.fetchTop10CloseLocations(request)

            if (response.isSuccessful && response.body() != null){
                //TODO: Handle empty body case, assign body to local val
                val toSaveEntities = response.body()!!.map { it.toEntity() }

                dao.insertLocations(toSaveEntities)
            }



        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }
}
