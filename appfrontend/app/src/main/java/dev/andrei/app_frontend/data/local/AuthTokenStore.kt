package dev.andrei.app_frontend.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = openPrefs(context)

    // AEADBadTagException (a GeneralSecurityException) means the Keystore master key no
    // longer matches the stored keyset -- the encrypted token is unrecoverable. Wipe the
    // corrupted keyset file + master key and rebuild a fresh store; the user is logged out
    // and signs in again, which is far better than crashing the app.
    private fun openPrefs(context: Context): SharedPreferences =
        try {
            buildEncryptedPrefs(context)
        } catch (e: GeneralSecurityException) {
            recoverAndRebuild(context)
        } catch (e: IOException) {
            recoverAndRebuild(context)
        }

    private fun recoverAndRebuild(context: Context): SharedPreferences {
        context.deleteSharedPreferences(PREFS_FILE_NAME) // drops both keyset + encrypted values
        deleteMasterKey()
        return buildEncryptedPrefs(context)              // regenerates a fresh keyset/master key
    }

    private fun buildEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun deleteMasterKey() {
        try {
            KeyStore.getInstance("AndroidKeyStore")
                .apply { load(null) }
                .deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        } catch (e: Exception) {
            // Best-effort: deleting the prefs file alone usually suffices; ignore failures.
        }
    }

    fun saveToken(token: String, expiresAt: Long) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun isLoggedIn(): Boolean {
        val token = getToken() ?: return false
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return token.isNotBlank() && expiresAt > System.currentTimeMillis()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_FILE_NAME = "auth_prefs"
        private const val KEY_TOKEN = "jwt"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}
