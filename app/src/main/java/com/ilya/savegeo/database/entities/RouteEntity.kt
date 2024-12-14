package com.ilya.savegeo.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "distance") val distance: Float = 0f, // Длина маршрута в метрах
    @ColumnInfo(name = "duration") val duration: Long = 0L,  // Время маршрута в секундах
    @ColumnInfo(name = "average_speed") val averageSpeed: Float = 0f // Средняя скорость в м/с
)

