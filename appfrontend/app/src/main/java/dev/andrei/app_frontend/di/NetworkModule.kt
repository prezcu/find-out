package dev.andrei.app_frontend.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.andrei.app_frontend.data.remote.ApiConfig
import dev.andrei.app_frontend.data.remote.AuthInterceptor
import dev.andrei.app_frontend.data.remote.api.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            // Backend queries hit remote Supabase (PostGIS + trigram search) and Render can
            // cold-start, so the default 10s read timeout aborts slow-but-valid responses.
            // Raise to 30s; callTimeout caps the whole call so it still can't hang forever.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl) // emulator -> 10.0.2.2; device -> BACKEND_BASE_URL (see ApiConfig)
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