package com.ilya.savegeo.location_tracking

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.ilya.savegeo.R
import com.ilya.savegeo.database.daos.LocationDao
import com.ilya.savegeo.database.entities.LocationEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    @Inject
    lateinit var locationDao: LocationDao
    private var routeId: Int = 0

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sendServiceState(true)

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("Отслеживание локации")
            .setContentText("Приложение отслеживает ваше местоположение")
            .setSmallIcon(R.drawable.ic_location)
            .build()
        startForeground(1, notification)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    saveLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val config = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("location_config", LocationConfig::class.java)
        } else {
            intent?.getParcelableExtra("location_config")
        }
        intent?.let { routeId = it.getIntExtra("route_id", 0) }
        config?.let { startLocationUpdates(it) }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sendServiceState(false)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(locationConfig: LocationConfig) {
        val locationRequest =
            LocationRequest.Builder(locationConfig.priority, locationConfig.interval)
                .setMinUpdateIntervalMillis(locationConfig.minUpdateInterval)
                .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun saveLocation(latitude: Double, longitude: Double) {
        val timestamp = System.currentTimeMillis()
        val locationEntity = LocationEntity(
            latitude = latitude,
            longitude = longitude,
            timestamp = timestamp,
            routeId = routeId
        )

        broadcastLocationUpdate(latitude, longitude)
        CoroutineScope(Dispatchers.IO).launch {
            locationDao.insertLocation(locationEntity)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "location_channel",
                "Location Tracking Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun sendServiceState(isActive: Boolean) {
        val intent = Intent("SERVICE_STATE_ACTION").apply {
            putExtra("isServiceActive", isActive)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastLocationUpdate(latitude: Double, longitude: Double) {
        val intent = Intent("LOCATION_UPDATE_ACTION").apply {
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
