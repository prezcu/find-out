package dev.andrei.app_frontend.data.remote

import dev.andrei.app_frontend.data.local.AuthTokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: AuthTokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        // Attach the JWT to authenticated endpoints: reviews (submit + /reviews/me), wishlist,
        // preferences, and the personalized recommended list. Public reads such as
        // /api/locations/nearby, /api/locations/search and /api/locations/{id}/reviews are left untouched.
        val needsAuth = path.startsWith("/api/reviews") ||
                path.startsWith("/api/wishlist") ||
                path.startsWith("/api/preferences") ||
                path == "/api/locations/recommended"
        if (!needsAuth) {
            return chain.proceed(original)
        }

        val token = tokenStore.getToken()
        val request = if (token != null) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
