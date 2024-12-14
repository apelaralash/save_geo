package com.ilya.savegeo.viewmodel

import androidx.lifecycle.ViewModel
import com.ilya.savegeo.database.daos.RouteDao
import com.ilya.savegeo.database.entities.RouteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class RouteStatsViewModel @Inject constructor(private val routeDao: RouteDao) : ViewModel() {

    fun getRouteById(routeId: Int): Flow<RouteEntity?> {
        return routeDao.getRouteById(routeId)
    }
}