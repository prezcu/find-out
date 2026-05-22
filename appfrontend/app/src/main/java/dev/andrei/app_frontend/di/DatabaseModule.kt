package dev.andrei.app_frontend.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.andrei.app_frontend.data.local.FindOutDatabase
import dev.andrei.app_frontend.data.local.dao.LocationDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): FindOutDatabase {
        return Room.databaseBuilder(
            context,
            FindOutDatabase::class.java, // giving room the blueprint
            "findout_db"
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideLocationDao(database: FindOutDatabase): LocationDao {
        // you just need to invoke the function/property from the database
        // you can use just this inside the repository if needed, granular injection
        return database.locationDao
    }
}