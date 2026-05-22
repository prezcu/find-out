package dev.andrei.app_frontend.data.service.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

class LocationTrackerImpl @Inject constructor(
    private val deviceLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
): LocationTracker {

    override suspend fun getDeviceCurrentLocation(
        priority: Int,
        token: CancellationToken
    ): Location? {

        val hasFineLocationAccessPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationAccessPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // TODO: check for every priority case and trigger gps permissionbox if necessary
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!hasFineLocationAccessPermission || !hasCoarseLocationAccessPermission || !isGpsEnabled) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            deviceLocationClient.getCurrentLocation(priority, token).apply {
                if (isComplete) {
                    if (isSuccessful) {
                        continuation.resume(result) { cause, _, _ -> }
                    } else {
                        continuation.resume(null) { cause, _, _ -> }
                    }
                    return@suspendCancellableCoroutine
                }

                addOnFailureListener { continuation.resume(null) { _, _, _ -> } }
                addOnSuccessListener { continuation.resume(it) { _, _, _ -> }}
                addOnCanceledListener { continuation.cancel() }
            }

        }

    }
}