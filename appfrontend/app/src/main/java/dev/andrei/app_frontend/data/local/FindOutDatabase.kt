package dev.andrei.app_frontend.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.andrei.app_frontend.data.local.dao.LocationDao
import dev.andrei.app_frontend.data.local.entity.LocationEntity

@Database(
    entities = [LocationEntity::class],
    version = 2
)
@TypeConverters(StringListConverter::class)
abstract class FindOutDatabase: RoomDatabase() {

    abstract val locationDao: LocationDao
}