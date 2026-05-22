package dev.andrei.app_frontend.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface LocationDao {

    @Upsert
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Query("DELETE FROM location")
    suspend fun clearLocations()

    @Query("SELECT * FROM location ORDER BY averageScore DESC LIMIT 10;")
    fun getLocationsOrderedByName(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM location WHERE id = :id")
    fun getLocationById(id: UUID): Flow<LocationEntity?>
}