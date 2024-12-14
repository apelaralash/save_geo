package com.ilya.savegeo.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ilya.savegeo.database.entities.LocationEntity

@Dao
interface LocationDao {

    @Insert
    fun insertLocation(location: LocationEntity)

    @Query("SELECT * FROM locations WHERE route_id = :routeId")
    fun getLocationsForRoute(routeId: Int): List<LocationEntity>
}
