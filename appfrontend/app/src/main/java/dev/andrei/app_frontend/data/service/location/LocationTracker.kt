package dev.andrei.app_frontend.data.service.location

import android.location.Location
import com.google.android.gms.tasks.CancellationToken
import kotlinx.coroutines.flow.Flow

interface LocationTracker {

    suspend fun getDeviceCurrentLocation(priority: Int, token: CancellationToken): Location?
}