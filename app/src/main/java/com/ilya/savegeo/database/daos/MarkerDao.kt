package com.ilya.savegeo.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ilya.savegeo.database.entities.MarkerEntity

@Dao
interface MarkerDao {
    @Insert
    fun insertMarker(marker: MarkerEntity)

    @Query("SELECT * FROM markers WHERE route_id = :routeId")
    fun getMarkersForRoute(routeId: Int): List<MarkerEntity>

    @Query("DELETE FROM markers WHERE id = :markerId")
    fun deleteMarker(markerId: Int)
}
