package com.ilya.savegeo.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ilya.savegeo.viewmodel.MainActivityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val routes by viewModel.routes.collectAsState(emptyList())
    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = {
            RouteDrawer(
                drawerState = drawerState,
                routes = routes,
                onSelectRoute = {
                    viewModel.selectRoute(it)
                    scope.launch {
                        drawerState.close()
                    }
                },
                onDeleteRoute = { viewModel.deleteRoute(it) },
                onEditRoute = { id, newName -> viewModel.editRouteName(id, newName) },
                onShowStats = { viewModel.showRouteStats(navController, it) }
            )
        },
        scrimColor = MaterialTheme.colorScheme.surface,
        gesturesEnabled = false
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SaveGeo") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open drawer")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LocationMapScreen(viewModel, Modifier.fillMaxSize())
            }
        }
    }
}
