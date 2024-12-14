package com.ilya.savegeo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ilya.savegeo.database.daos.LocationDao
import com.ilya.savegeo.database.daos.MarkerDao
import com.ilya.savegeo.database.daos.RouteDao
import com.ilya.savegeo.database.entities.LocationEntity
import com.ilya.savegeo.database.entities.MarkerEntity
import com.ilya.savegeo.database.entities.RouteEntity

@Database(
    entities = [LocationEntity::class, RouteEntity::class, MarkerEntity::class], version = 2
)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun routeDao(): RouteDao
    abstract fun markerDao(): MarkerDao

    companion object {
        @Volatile
        private var INSTANCE: LocationDatabase? = null

        fun getDatabase(context: Context): LocationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, LocationDatabase::class.java, "location_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE locations")
                database.execSQL(
                    """
                    CREATE TABLE routes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        start_time INTEGER NOT NULL,
                        distance REAL DEFAULT 0 NOT NULL,
                        duration INTEGER DEFAULT 0 NOT NULL,
                        average_speed REAL DEFAULT 0 NOT NULL
                    )
                    """
                )

                database.execSQL(
                    """
                    CREATE TABLE locations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        route_id INTEGER NOT NULL,
                        FOREIGN KEY(route_id) REFERENCES routes(id) ON DELETE CASCADE
                    )
                    """
                )

                database.execSQL("CREATE INDEX index_locations_route_id ON locations(route_id)")

                database.execSQL(
                    """
                    CREATE TABLE markers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        name TEXT DEFAULT '' NOT NULL,
                        description TEXT NOT NULL,
                        route_id INTEGER NOT NULL,
                        FOREIGN KEY(route_id) REFERENCES routes(id) ON DELETE CASCADE
                    )
                    """
                )

                database.execSQL("CREATE INDEX index_markers_route_id ON markers(route_id)")
            }
        }
    }
}

