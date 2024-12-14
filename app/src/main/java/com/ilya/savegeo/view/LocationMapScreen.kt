package com.ilya.savegeo.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.ilya.savegeo.R
import com.ilya.savegeo.viewmodel.MainActivityViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationMapScreen(
    viewModel: MainActivityViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val locationPermissionState =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current

    val location by viewModel.locationState.collectAsState()
    val isLocationTracked by viewModel.isServiceActive.collectAsState()
    val markerState = rememberMarkerState()
    var showDialog by remember { mutableStateOf(false) }
    val recordedLocations by viewModel.path.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val markers by viewModel.selectedRouteMarkers.collectAsState()
    var isInitialCameraPositionSet by remember { mutableStateOf(false) }

    var showAddMarkerDialog by remember { mutableStateOf(false) }
    var markerLatLng by remember { mutableStateOf<LatLng?>(null) }
    var markerName by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose { isInitialCameraPositionSet = false }
    }

    DisposableEffect(Unit) {
        viewModel.startLocationTracking()
        onDispose { viewModel.stopLocationTracking() }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val isServiceActive = intent.getBooleanExtra("isServiceActive", false)
                viewModel.setServiceState(isServiceActive)
            }
        }
        val filter = IntentFilter("SERVICE_STATE_ACTION")
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)

        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    LaunchedEffect(selectedRoute) {
        if (selectedRoute != null) {
            viewModel.updatePath(selectedRoute!!.id)
            viewModel.updateMarkers(selectedRoute!!.id)
        } else {
            viewModel.cleanPath()
            viewModel.cleanMarkers()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
    }

    ChoiceDialog(
        showDialog = showDialog,
        onConfirm = { locationConfig ->
            showDialog = false
            viewModel.onStartWritingLocationClicked(context, locationConfig)
        },
        onDismiss = { showDialog = false }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissionState.status.isGranted) {
            location?.let { location ->
                val currentLatLng = LatLng(location.latitude, location.longitude)
                LaunchedEffect(currentLatLng) {
                    if (!isInitialCameraPositionSet) {
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(currentLatLng, 15f)
                        isInitialCameraPositionSet = true
                    }
                }

                LaunchedEffect(recordedLocations) {
                    if (recordedLocations.isNotEmpty()) {
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(recordedLocations[0], 15f)
                    }
                }

                LaunchedEffect(currentLatLng) {
                    markerState.position = currentLatLng
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapLongClick = { latLng ->
                        if (selectedRoute != null) {
                            markerLatLng = latLng
                            markerName = ""
                            showAddMarkerDialog = true
                        } else {
                            Toast.makeText(
                                context,
                                "Выберите маршрут для добавления меток",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    markers.forEach {
                        Marker(
                            state = rememberMarkerState(position = LatLng(it.latitude, it.longitude)),
                            title = it.name,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        )
                    }

                    Marker(
                        state = markerState,
                        title = "Вы здесь",
                        icon = bitmapDescriptorFromVector(context, R.drawable.ic_current_location)
                    )

                    if (recordedLocations.isNotEmpty()) {
                        Polyline(
                            points = recordedLocations,
                            color = Color.Blue,
                            width = 5f
                        )
                    }
                }

                IconButton(
                    onClick = {
                        currentLatLng.let { latLng ->
                            cameraPositionState.position =
                                CameraPosition.fromLatLngZoom(latLng, 15f)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_location_targer),
                        contentDescription = "Возврат к местоположению",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                if (isLocationTracked) {
                    Button(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        onClick = { viewModel.onStopLocationWritingClicked(context) }
                    ) {
                        Text("Остановить запись геолокации")
                    }
                } else {
                    Button(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        onClick = { showDialog = true }
                    ) {
                        Text("Начать запись геолокации")
                    }
                }
            }
        } else {
            Button(
                modifier = Modifier.align(Alignment.Center),
                onClick = { locationPermissionState.launchPermissionRequest() }
            ) {
                Text("Запросить разрешение на геолокацию")
            }
        }


        if (showAddMarkerDialog && markerLatLng != null) {
            AddMarkerDialog(
                markerName = markerName,
                onNameChange = { markerName = it },
                onConfirm = {
                    markerLatLng?.let { latLng ->
                        viewModel.addMarkerToRoute(
                            routeId = selectedRoute?.id!!,
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            name = markerName,
                            description = "Метка (${latLng.latitude}, ${latLng.longitude})",
                        )
                    }
                    showAddMarkerDialog = false
                },
                onDismiss = { showAddMarkerDialog = false }
            )
        }
    }
}


@Composable
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
    val bitmap =
        createColoredMarkerBitmap(context, vectorResId, MaterialTheme.colorScheme.primary.toArgb())
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun createColoredMarkerBitmap(context: Context, resourceId: Int, color: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, resourceId) ?: throw IllegalArgumentException(
        "Resource not found"
    )

    val width = drawable.intrinsicWidth
    val height = drawable.intrinsicHeight

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val colorFilter: ColorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    drawable.colorFilter = colorFilter

    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)

    return bitmap
}
