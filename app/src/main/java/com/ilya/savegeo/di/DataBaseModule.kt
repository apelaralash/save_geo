package com.ilya.savegeo.di

import android.content.Context
import com.ilya.savegeo.database.LocationDatabase
import com.ilya.savegeo.database.daos.LocationDao
import com.ilya.savegeo.database.daos.MarkerDao
import com.ilya.savegeo.database.daos.RouteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataBaseModule {
    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): LocationDatabase {
            return LocationDatabase.getDatabase(context)
        }

        @Provides
        fun provideLocationDao(database: LocationDatabase): LocationDao {
            return database.locationDao()
        }

        @Provides
        fun provideRoutesDao(database: LocationDatabase): RouteDao {
            return database.routeDao()
        }

        @Provides
        fun provideMarkersDao(database: LocationDatabase): MarkerDao {
            return database.markerDao()
        }
    }
}