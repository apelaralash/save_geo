package com.ilya.savegeo.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilya.savegeo.viewmodel.RouteStatsViewModel

@Composable
fun RouteStatsScreen(routeId: Int, viewModel: RouteStatsViewModel = hiltViewModel()) {
    val route by viewModel.getRouteById(routeId).collectAsState(initial = null)

    if (route != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(16.dp),
        ) {
            Text(
                "Статистика маршрута",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Название: ${route!!.name}", color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Длина маршрута: ${route!!.distance} м",
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Время маршрута: ${route!!.duration / 60} мин",
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Средняя скорость: ${route!!.averageSpeed} м/с",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        Text(
            "Маршрут не найден",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
