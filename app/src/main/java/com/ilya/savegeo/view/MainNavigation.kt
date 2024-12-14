package com.ilya.savegeo.view

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen(navController)
        }
        composable("route_stats_screen/{routeId}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId")?.toInt() ?: -1
            RouteStatsScreen(routeId = routeId)
        }
    }
}
