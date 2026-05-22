package dev.andrei.app_frontend.data.repository

import android.location.Location
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import dev.andrei.app_frontend.data.service.location.LocationTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val locationTracker: LocationTracker
): SessionRepository {

    private val _permissionGranted = MutableStateFlow(false)
    override val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    private val _deviceLocation = MutableStateFlow<Location?>(null)
    override val deviceLocation: StateFlow<Location?> = _deviceLocation.asStateFlow()

    override fun onPermissionResult(isGranted: Boolean) {
        _permissionGranted.value = isGranted
    }

    override suspend fun updateDeviceLocation() {
        _deviceLocation.value = locationTracker.getDeviceCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token)
    }

}