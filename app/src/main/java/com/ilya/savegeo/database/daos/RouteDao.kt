package com.ilya.savegeo.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ilya.savegeo.database.entities.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Insert
    fun insertRoute(route: RouteEntity): Long

    @Query("SELECT * FROM routes")
    fun getAllRoutesFlow(): Flow<List<RouteEntity>>

    @Query("DELETE FROM routes WHERE id = :routeId")
    fun deleteRoute(routeId: Int)

    @Query("UPDATE routes SET distance = :distance, duration = :duration, average_speed = :averageSpeed WHERE id = :routeId")
    fun updateRouteStats(routeId: Int, distance: Float, duration: Long, averageSpeed: Float)

    @Query("SELECT * FROM routes ORDER BY id DESC LIMIT 1")
    fun getLastCreatedRoute(): RouteEntity?

    @Query("UPDATE routes SET name = :newName WHERE id = :routeId")
    fun updateRouteName(routeId: Int, newName: String)

    @Query("SELECT * FROM routes WHERE id = :routeId")
    fun getRouteById(routeId: Int): Flow<RouteEntity?>
}