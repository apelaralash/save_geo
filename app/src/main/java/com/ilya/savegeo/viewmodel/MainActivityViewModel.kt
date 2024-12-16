package com.ilya.savegeo.viewmodel

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.LatLng
import com.ilya.savegeo.database.daos.LocationDao
import com.ilya.savegeo.database.daos.MarkerDao
import com.ilya.savegeo.database.daos.RouteDao
import com.ilya.savegeo.database.entities.LocationEntity
import com.ilya.savegeo.database.entities.MarkerEntity
import com.ilya.savegeo.database.entities.RouteEntity
import com.ilya.savegeo.location_tracking.LocationConfig
import com.ilya.savegeo.location_tracking.LocationTracker
import com.ilya.savegeo.location_tracking.LocationTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val locationTracker: LocationTracker,
    private val locationDao: LocationDao,
    private val routeDao: RouteDao,
    private val markerDao: MarkerDao,
) : ViewModel() {
    private val _isServiceActive = MutableStateFlow(false)
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    val locationState: StateFlow<Location?> = locationTracker.locationFlow

    private val _path = MutableStateFlow<List<LatLng>>(emptyList())
    val path: StateFlow<List<LatLng>> = _path.asStateFlow()

    val routes = routeDao.getAllRoutesFlow()
    private val _selectedRoute = MutableStateFlow<RouteEntity?>(null)
    val selectedRoute = _selectedRoute.asStateFlow()

    private val _selectedRouteMarkers = MutableStateFlow<List<MarkerEntity>>(emptyList())
    val selectedRouteMarkers = _selectedRouteMarkers.asStateFlow()

    private val _pathIsUpdated = MutableStateFlow(false)
    val pathIsUpdated = _pathIsUpdated.asStateFlow()

    fun setServiceState(isActive: Boolean) {
        viewModelScope.launch {
            _isServiceActive.value = isActive
        }
    }

    fun startLocationTracking() {
        locationTracker.startLocationUpdates()
    }

    fun stopLocationTracking() {
        locationTracker.stopLocationUpdates()
    }

    fun onStartWritingLocationClicked(context: Context, locationConfig: LocationConfig) {
        viewModelScope.launch {
            val routeId = withContext(Dispatchers.IO) {
                val currentTime = System.currentTimeMillis()
                val routeName = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).format(Date(currentTime))

                val newRoute = RouteEntity(
                    name = routeName,
                    startTime = currentTime
                )

                routeDao.insertRoute(newRoute)

                routeDao.getLastCreatedRoute()?.id
            }

            withContext(Dispatchers.Main) {
                val intent = Intent(context, LocationTrackingService::class.java).apply {
                    putExtra("location_config", locationConfig as Parcelable)
                    putExtra("route_id", routeId)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }

            val lastRoute = withContext(Dispatchers.IO) {
                routeDao.getLastCreatedRoute()
            }

            selectRoute(lastRoute)
        }
    }


    fun onStopLocationWritingClicked(context: Context) {
        val intent = Intent(context, LocationTrackingService::class.java)
        context.stopService(intent)

        viewModelScope.launch {
            val lastRoute = withContext(Dispatchers.IO) {
                routeDao.getLastCreatedRoute()
            }

            if (lastRoute != null) {
                val locations = withContext(Dispatchers.IO) {
                    locationDao.getLocationsForRoute(lastRoute.id)
                }

                _path.value = locations.map {
                    LatLng(it.latitude, it.longitude)
                }

                _pathIsUpdated.value = true

                if (locations.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        val totalDistance = calculateTotalDistance(locations)
                        val duration = calculateDuration(locations)
                        val averageSpeed = if (duration > 0) totalDistance / duration else 0f

                        routeDao.updateRouteStats(
                            routeId = lastRoute.id,
                            distance = totalDistance,
                            duration = duration,
                            averageSpeed = averageSpeed
                        )
                    }
                }
            }
        }
    }

    fun onNewLocationReceived(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedPath = _path.value.toMutableList()
            updatedPath.add(LatLng(latitude, longitude))
            _path.value = updatedPath
        }
    }

    fun selectRoute(route: RouteEntity?) {
        if (_selectedRoute.value == route) {
            _selectedRoute.value = null
        } else {
            _selectedRoute.value = route
        }
    }

    fun deleteRoute(routeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            routeDao.deleteRoute(routeId)
        }
    }

    fun editRouteName(routeId: Int, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            routeDao.updateRouteName(routeId, newName)
        }
    }

    fun updatePath(routeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _path.value = locationDao.getLocationsForRoute(routeId).map {
                LatLng(it.latitude, it.longitude)
            }
            _pathIsUpdated.value = true
        }
    }

    fun setPathUpdatedFalse() {
        _pathIsUpdated.value = false
    }

    fun updateMarkers(routeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedRouteMarkers.value = markerDao.getMarkersForRoute(routeId)
        }
    }

    fun cleanPath() {
        _path.value = emptyList()
    }

    fun cleanMarkers() {
        _selectedRouteMarkers.value = emptyList()
    }

    fun showRouteStats(navController: NavHostController, routeId: Int) {
        navController.navigate("route_stats_screen/$routeId")
    }

    fun addMarkerToRoute(
        routeId: Int,
        latitude: Double,
        longitude: Double,
        name: String,
        description: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val marker = MarkerEntity(
                latitude = latitude,
                longitude = longitude,
                name = name,
                description = description,
                routeId = routeId
            )
            markerDao.insertMarker(marker)
            _selectedRouteMarkers.value = markerDao.getMarkersForRoute(routeId)
        }
    }

    private fun calculateTotalDistance(locations: List<LocationEntity>): Float {
        var totalDistance = 0f
        for (i in 1 until locations.size) {
            val start = locations[i - 1]
            val end = locations[i]
            totalDistance += calculateDistance(
                start.latitude, start.longitude,
                end.latitude, end.longitude
            )
        }
        return totalDistance
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    private fun calculateDuration(locations: List<LocationEntity>): Long {
        val startTime = locations.firstOrNull()?.timestamp ?: return 0
        val endTime = locations.lastOrNull()?.timestamp ?: return 0
        return (endTime - startTime) / 1000
    }
}