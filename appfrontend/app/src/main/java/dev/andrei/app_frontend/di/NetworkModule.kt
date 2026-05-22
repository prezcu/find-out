package dev.andrei.app_frontend.di

import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.andrei.app_frontend.BuildConfig
import dev.andrei.app_frontend.data.remote.AuthInterceptor
import dev.andrei.app_frontend.data.remote.api.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    // Helper function to detect if the app is running on an emulator
    fun isRunningOnEmulator(): Boolean {
        // Build.HARDWARE is "ranchu" (modern QEMU) or "goldfish" (legacy QEMU) on every
        // Android Studio emulator — the most reliable signal across API levels.
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

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val BASE_URL = if (isRunningOnEmulator()) {
            BuildConfig.EMULATOR_BASE_URL
        } else {
            BuildConfig.BACKEND_BASE_URL
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL) // URL from build.gradle or ngrok url
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        // Hilt provides the 'retrofit' object from the recipe above
        return retrofit.create(ApiService::class.java)
    }
}