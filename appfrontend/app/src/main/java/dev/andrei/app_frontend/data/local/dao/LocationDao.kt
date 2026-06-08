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

    @Query("SELECT * FROM location WHERE id = :id")
    fun getLocationById(id: UUID): Flow<LocationEntity?>
}