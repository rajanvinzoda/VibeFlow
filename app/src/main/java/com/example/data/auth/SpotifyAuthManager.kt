package com.example.data.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.example.data.api.SpotifyAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.MessageDigest
import java.security.SecureRandom

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticating : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class SpotifyAuthManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("vibeflow_auth_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val authService: SpotifyAuthService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SpotifyAuthService::class.java)
    }

    init {
        // Clear any old fake/demo token from disk so user must login with real Spotify
        val saved = prefs.getString("access_token", null)
        if (saved != null && (saved.startsWith("demo") || saved.length < 30)) {
            prefs.edit().clear().apply()
        }
        prefs.edit().remove("is_demo_mode").apply()
        checkInitialAuth()
    }

    private fun checkInitialAuth() {
        val token = getAccessToken()
        if (!token.isNullOrBlank()) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun getClientId(): String {
        val custom = prefs.getString("custom_client_id", null)
        if (!custom.isNullOrBlank()) return custom.trim()

        return try {
            val buildConfigId = BuildConfig.SPOTIFY_CLIENT_ID
            if (buildConfigId.isNotBlank() && buildConfigId != "your_spotify_client_id") {
                buildConfigId
            } else {
                "cd36d1e5a67345588f4568095ae4845e"
            }
        } catch (e: Exception) {
            "cd36d1e5a67345588f4568095ae4845e"
        }
    }

    fun saveCustomClientId(id: String) {
        prefs.edit().putString("custom_client_id", id.trim()).apply()
    }

    fun getRedirectUri(): String {
        val custom = prefs.getString("custom_redirect_uri", null)
        if (!custom.isNullOrBlank()) return custom.trim()

        return try {
            val uri = BuildConfig.SPOTIFY_REDIRECT_URI
            if (uri.isNotBlank() && uri.startsWith("http")) {
                uri
            } else {
                "https://ais-dev-ltjpcfckdf7cu6mtlvri2p-81918527203.asia-southeast1.run.app/callback"
            }
        } catch (e: Exception) {
            "https://ais-dev-ltjpcfckdf7cu6mtlvri2p-81918527203.asia-southeast1.run.app/callback"
        }
    }

    fun saveCustomRedirectUri(uri: String) {
        prefs.edit().putString("custom_redirect_uri", uri.trim()).apply()
    }

    fun buildAuthorizeUrl(): String? {
        val clientId = getClientId()
        if (clientId.isBlank()) {
            _authState.value = AuthState.Error(
                "Please configure your Spotify Client ID first in Settings or Secrets."
            )
            return null
        }

        val codeVerifier = generateCodeVerifier()
        prefs.edit().putString("code_verifier", codeVerifier).apply()

        val codeChallenge = generateCodeChallenge(codeVerifier)
        val redirectUri = getRedirectUri()

        val scopes = listOf(
            "user-read-private",
            "user-read-email",
            "playlist-read-private",
            "playlist-read-collaborative",
            "playlist-modify-public",
            "playlist-modify-private",
            "user-library-read",
            "user-library-modify",
            "user-top-read",
            "user-read-recently-played",
            "user-read-playback-state",
            "user-modify-playback-state",
            "user-read-currently-playing",
            "user-follow-read"
        ).joinToString(" ")

        return Uri.parse("https://accounts.spotify.com/authorize").buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("scope", scopes)
            .appendQueryParameter("show_dialog", "true")
            .build()
            .toString()
    }

    fun isRedirectUrl(uri: Uri): Boolean {
        val targetUri = Uri.parse(getRedirectUri())
        if (uri.scheme == targetUri.scheme && uri.host == targetUri.host) {
            return uri.getQueryParameter("code") != null || uri.getQueryParameter("error") != null || uri.path?.contains("callback") == true
        }
        if (uri.scheme == "vibeflow" && uri.host == "callback") return true
        if (uri.path?.contains("/callback") == true) return true
        return false
    }

    fun buildAuthorizationIntent(): Intent? {
        val url = buildAuthorizeUrl() ?: return null
        _authState.value = AuthState.Authenticating
        return Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    suspend fun handleCallback(uri: Uri): Boolean {
        val error = uri.getQueryParameter("error")
        if (error != null) {
            _authState.value = AuthState.Error("Spotify login failed or was cancelled: $error")
            return false
        }

        val code = uri.getQueryParameter("code")
        if (code == null) {
            _authState.value = AuthState.Error("Invalid authorization response from Spotify.")
            return false
        }

        val codeVerifier = prefs.getString("code_verifier", null)
        if (codeVerifier == null) {
            _authState.value = AuthState.Error("Code verifier not found. Please try logging in again.")
            return false
        }

        val clientId = getClientId()
        val redirectUri = getRedirectUri()

        return try {
            val response = authService.exchangeCodeForToken(
                clientId = clientId,
                code = code,
                redirectUri = redirectUri,
                codeVerifier = codeVerifier
            )

            val expiresAt = System.currentTimeMillis() + (response.expiresIn * 1000L)
            saveTokens(response.accessToken, response.refreshToken, expiresAt)
            _authState.value = AuthState.Authenticated
            true
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Authentication error: ${e.localizedMessage ?: "Unknown error"}")
            false
        }
    }

    suspend fun refreshAccessTokenIfNeeded(): String? {
        val token = getAccessToken() ?: return null
        val expiresAt = prefs.getLong("token_expires_at", 0L)

        // If expires in less than 2 minutes, refresh it
        if (System.currentTimeMillis() + 120_000L >= expiresAt) {
            val refreshToken = prefs.getString("refresh_token", null) ?: return token
            val clientId = getClientId()
            try {
                val resp = authService.refreshToken(
                    clientId = clientId,
                    refreshToken = refreshToken
                )
                val newExpiresAt = System.currentTimeMillis() + (resp.expiresIn * 1000L)
                saveTokens(resp.accessToken, resp.refreshToken ?: refreshToken, newExpiresAt)
                return resp.accessToken
            } catch (e: Exception) {
                // Return current token if refresh failed temporarily
                return token
            }
        }
        return token
    }

    fun getAccessToken(): String? {
        val token = prefs.getString("access_token", null)
        if (token.isNullOrBlank() || token.startsWith("demo") || token.length < 30) {
            return null
        }
        return token
    }

    private fun saveTokens(accessToken: String, refreshToken: String?, expiresAt: Long) {
        prefs.edit()
            .putString("access_token", accessToken)
            .apply {
                if (refreshToken != null) putString("refresh_token", refreshToken)
            }
            .putLong("token_expires_at", expiresAt)
            .apply()
    }

    fun logout() {
        prefs.edit()
            .remove("access_token")
            .remove("refresh_token")
            .remove("token_expires_at")
            .remove("code_verifier")
            .remove("is_demo_mode")
            .apply()
        _authState.value = AuthState.Unauthenticated
    }

    private fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val code = ByteArray(64)
        secureRandom.nextBytes(code)
        return Base64.encodeToString(
            code,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    private fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val md = MessageDigest.getInstance("SHA-256")
        md.update(bytes, 0, bytes.size)
        val digest = md.digest()
        return Base64.encodeToString(
            digest,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }
}
