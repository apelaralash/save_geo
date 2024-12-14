package com.ilya.savegeo.di

import android.content.Context
import com.ilya.savegeo.location_tracking.LocationTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocationTrackerModule {

    companion object {
        @Provides
        @Singleton
        fun provideLocationTracker(@ApplicationContext context: Context): LocationTracker {
            return LocationTracker(context)
        }
    }
}