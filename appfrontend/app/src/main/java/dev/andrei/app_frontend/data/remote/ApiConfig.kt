package dev.andrei.app_frontend.data.remote

import android.os.Build
import dev.andrei.app_frontend.BuildConfig

/**
 * Single source of truth for the backend base URL and for building absolute image URLs.
 *
 * Coil needs an absolute URL to load a photo, and the backend can't know its own externally
 * reachable host (the emulator reaches the host machine via 10.0.2.2). So the host is decided
 * here, on the device, exactly like Retrofit's base URL — and [photoUrl] reuses it.
 */
object ApiConfig {

    /**
     * True when running on an Android Studio emulator. Build.HARDWARE is "ranchu" (modern QEMU)
     * or "goldfish" (legacy QEMU) on every emulator — the most reliable signal across API levels.
     */
    fun isRunningOnEmulator(): Boolean {
        if (Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish") return true

        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.FINGERPRINT.contains("generic")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MODEL.contains("sdk_gphone")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.PRODUCT.startsWith("sdk_")
                || Build.PRODUCT.contains("_sdk")
                || "google_sdk" == Build.PRODUCT)
    }

    /** Base URL ending in a trailing slash, matching Retrofit's expectation. */
    val baseUrl: String
        get() = if (isRunningOnEmulator()) BuildConfig.EMULATOR_BASE_URL else BuildConfig.BACKEND_BASE_URL

    /**
     * Absolute URL for the photo at [index] of a location. The backend 302-redirects this to a
     * fresh, key-less Google image URL; the API key never reaches the device.
     */
    fun photoUrl(locationId: String, index: Int = 0): String =
        "${baseUrl}api/locations/$locationId/photo?index=$index"
}
