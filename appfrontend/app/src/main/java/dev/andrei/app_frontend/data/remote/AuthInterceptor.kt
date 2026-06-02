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

        // token attached only to the submit review request
        // so far only login-required feature
        if (!path.startsWith("/api/reviews")) {
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
