package dev.andrei.app_frontend.data.repository

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {

    val permissionGranted: StateFlow<Boolean>

    val deviceLocation: StateFlow<Location?>

    fun onPermissionResult(isGranted: Boolean)

    suspend fun updateDeviceLocation()
}