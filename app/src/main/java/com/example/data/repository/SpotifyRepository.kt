package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.api.SpotifyApiService
import com.example.data.auth.SpotifyAuthManager
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class SpotifyRepository(
    private val context: Context,
    private val authManager: SpotifyAuthManager
) {
    private val _currentUser = MutableStateFlow<SpotifyUser?>(null)
    val currentUser: StateFlow<SpotifyUser?> = _currentUser.asStateFlow()

    private val _userPlaylists = MutableStateFlow<List<SpotifyPlaylist>>(emptyList())
    val userPlaylists: StateFlow<List<SpotifyPlaylist>> = _userPlaylists.asStateFlow()

    private val _savedTracks = MutableStateFlow<List<SpotifyTrack>>(emptyList())
    val savedTracks: StateFlow<List<SpotifyTrack>> = _savedTracks.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<SpotifyTrack>>(emptyList())
    val recentlyPlayed: StateFlow<List<SpotifyTrack>> = _recentlyPlayed.asStateFlow()

    private val _topTracks = MutableStateFlow<List<SpotifyTrack>>(emptyList())
    val topTracks: StateFlow<List<SpotifyTrack>> = _topTracks.asStateFlow()

    private val _topArtists = MutableStateFlow<List<SpotifyArtist>>(emptyList())
    val topArtists: StateFlow<List<SpotifyArtist>> = _topArtists.asStateFlow()

    private val _followedArtists = MutableStateFlow<List<SpotifyArtist>>(emptyList())
    val followedArtists: StateFlow<List<SpotifyArtist>> = _followedArtists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val apiService: SpotifyApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = authManager.getAccessToken()
            val requestBuilder = original.newBuilder()
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SpotifyApiService::class.java)
    }

    suspend fun refreshAllLibraryData() {
        _isLoading.value = true
        _errorMessage.value = null

        // Make sure we have a fresh token before starting API calls
        val freshToken = authManager.refreshAccessTokenIfNeeded()
        if (freshToken.isNullOrBlank()) {
            _isLoading.value = false
            _errorMessage.value = "No active Spotify login session. Please log in."
            authManager.logout()
            return
        }

        try {
            // 1. Current user profile (real user from Spotify)
            try {
                val user = apiService.getCurrentUser()
                _currentUser.value = user
                Log.d("SpotifyRepository", "Loaded real user: ${user.displayName} (${user.id})")
            } catch (e: Exception) {
                Log.e("SpotifyRepository", "Failed to get user profile: ${e.message}", e)
                if (e is retrofit2.HttpException && e.code() == 401) {
                    _errorMessage.value = "Spotify session expired. Please log in again."
                    authManager.logout()
                    return
                } else if (e is retrofit2.HttpException && e.code() == 403) {
                    _errorMessage.value = "Spotify 403 Forbidden: In Developer Mode, please add your email in User Management in the Developer Dashboard."
                } else {
                    _errorMessage.value = "User Profile Error: ${e.message}"
                }
            }

            // 2. Playlists
            try {
                val playlistsResp = apiService.getCurrentUserPlaylists(limit = 50)
                _userPlaylists.value = playlistsResp.items
                Log.d("SpotifyRepository", "Loaded playlists: ${playlistsResp.items.size}")
            } catch (e: Exception) {
                Log.e("SpotifyRepository", "Failed to get playlists: ${e.message}", e)
            }

            // 3. Saved / Liked Tracks
            try {
                val savedResp = apiService.getSavedTracks(limit = 50)
                _savedTracks.value = savedResp.items.map { it.track }
                Log.d("SpotifyRepository", "Loaded saved tracks: ${savedResp.items.size}")
            } catch (e: Exception) {
                Log.e("SpotifyRepository", "Failed to get saved tracks: ${e.message}", e)
            }

            // 4. Recently Played
            try {
                val recentResp = apiService.getRecentlyPlayed(limit = 30)
                _recentlyPlayed.value = recentResp.items.map { it.track }
            } catch (e: Exception) {
                Log.w("SpotifyRepository", "Recently played empty or error: ${e.message}")
            }

            // 5. Top Tracks
            try {
                val topTracksResp = apiService.getTopTracks(limit = 30)
                _topTracks.value = topTracksResp.items
            } catch (e: Exception) {
                Log.w("SpotifyRepository", "Top tracks empty or error: ${e.message}")
            }

            // 6. Top Artists
            try {
                val topArtistsResp = apiService.getTopArtists(limit = 30)
                _topArtists.value = topArtistsResp.items
            } catch (e: Exception) {
                Log.w("SpotifyRepository", "Top artists empty or error: ${e.message}")
            }

            // 7. Followed Artists
            try {
                val followedResp = apiService.getFollowedArtists(limit = 30)
                _followedArtists.value = followedResp.artists.items
            } catch (e: Exception) {
                Log.w("SpotifyRepository", "Followed artists empty or error: ${e.message}")
            }

        } catch (e: Exception) {
            val isAuthError = e is retrofit2.HttpException && e.code() == 401
            val isForbidden = e is retrofit2.HttpException && e.code() == 403
            val msg = when {
                isAuthError -> "Spotify token expired. Please log in again."
                isForbidden -> "Spotify 403 Forbidden: In Developer Mode, please add your email in User Management in the Developer Dashboard."
                else -> "Failed to load Spotify library: ${e.localizedMessage ?: e.message}"
            }
            _errorMessage.value = msg
            Log.e("SpotifyRepository", "Critical failure loading library: $msg", e)
            if (isAuthError) {
                authManager.logout()
            }
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun getPlaylistTracks(playlistId: String): List<SpotifyTrack> {
        return try {
            val resp = apiService.getPlaylistTracks(playlistId, limit = 50)
            resp.items.mapNotNull { it.track }
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Failed to load playlist $playlistId: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun search(query: String): SpotifySearchResponse {
        if (query.isBlank()) {
            return SpotifySearchResponse()
        }
        return try {
            apiService.search(query = query, limit = 20)
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Search failed: ${e.message}", e)
            SpotifySearchResponse()
        }
    }

    suspend fun toggleLikeTrack(track: SpotifyTrack): Boolean {
        val currentlyLiked = _savedTracks.value.any { it.id == track.id }
        val newLikedState = !currentlyLiked

        if (newLikedState) {
            _savedTracks.value = listOf(track) + _savedTracks.value.filter { it.id != track.id }
        } else {
            _savedTracks.value = _savedTracks.value.filter { it.id != track.id }
        }

        try {
            if (newLikedState) {
                apiService.saveTrack(track.id)
            } else {
                apiService.removeSavedTrack(track.id)
            }
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Failed to toggle like: ${e.message}", e)
        }
        return newLikedState
    }

    fun isTrackLiked(trackId: String): Boolean {
        return _savedTracks.value.any { it.id == trackId }
    }
}
