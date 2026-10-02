package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthState
import com.example.data.auth.SpotifyAuthManager
import com.example.data.model.*
import com.example.data.repository.SpotifyRepository
import com.example.player.VibeFlowPlayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenRoute {
    LOGIN,
    HOME,
    SEARCH,
    LIBRARY,
    LIKED,
    PLAYLIST_DETAIL,
    QUEUE,
    SETTINGS
}

class VibeFlowViewModel(application: Application) : AndroidViewModel(application) {

    val authManager = SpotifyAuthManager(application)
    val repository = SpotifyRepository(application, authManager)
    val playerManager = VibeFlowPlayerManager(
        context = application,
        authManager = authManager,
        apiService = { null }
    )

    private val _currentScreen = MutableStateFlow(ScreenRoute.LOGIN)
    val currentScreen: StateFlow<ScreenRoute> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<ScreenRoute>()

    private val _selectedPlaylist = MutableStateFlow<SpotifyPlaylist?>(null)
    val selectedPlaylist: StateFlow<SpotifyPlaylist?> = _selectedPlaylist.asStateFlow()

    private val _playlistTracks = MutableStateFlow<List<SpotifyTrack>>(emptyList())
    val playlistTracks: StateFlow<List<SpotifyTrack>> = _playlistTracks.asStateFlow()

    private val _isLoadingPlaylistTracks = MutableStateFlow(false)
    val isLoadingPlaylistTracks: StateFlow<Boolean> = _isLoadingPlaylistTracks.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow(SpotifySearchResponse())
    val searchResults: StateFlow<SpotifySearchResponse> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    // Library tab
    private val _selectedLibraryTab = MutableStateFlow(0) // 0: Playlists, 1: Songs, 2: Artists, 3: Albums
    val selectedLibraryTab: StateFlow<Int> = _selectedLibraryTab.asStateFlow()

    init {
        viewModelScope.launch {
            authManager.authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        _currentScreen.value = ScreenRoute.HOME
                        repository.refreshAllLibraryData()
                    }
                    is AuthState.Unauthenticated, is AuthState.Error -> {
                        _currentScreen.value = ScreenRoute.LOGIN
                    }
                    is AuthState.Authenticating -> {
                        // Keep on login screen with loading indicator
                    }
                }
            }
        }
    }

    fun navigateTo(route: ScreenRoute) {
        if (_currentScreen.value != route) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = route
        }
    }

    fun navigateBack(): Boolean {
        if (_isNowPlayingExpanded.value) {
            _isNowPlayingExpanded.value = false
            return true
        }
        if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.size - 1)
            return true
        }
        return false
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun setLibraryTab(tabIndex: Int) {
        _selectedLibraryTab.value = tabIndex
    }

    fun openPlaylist(playlist: SpotifyPlaylist) {
        _selectedPlaylist.value = playlist
        _isLoadingPlaylistTracks.value = true
        navigateTo(ScreenRoute.PLAYLIST_DETAIL)

        viewModelScope.launch {
            val tracks = repository.getPlaylistTracks(playlist.id)
            _playlistTracks.value = tracks
            _isLoadingPlaylistTracks.value = false
        }
    }

    private val _authUrlForWebView = MutableStateFlow<String?>(null)
    val authUrlForWebView: StateFlow<String?> = _authUrlForWebView.asStateFlow()

    fun openInAppLogin() {
        val url = authManager.buildAuthorizeUrl()
        if (url != null) {
            _authUrlForWebView.value = url
        }
    }

    fun dismissInAppLogin() {
        _authUrlForWebView.value = null
    }

    fun onAuthCodeReceivedFromWebView(uri: Uri) {
        _authUrlForWebView.value = null
        handleAuthCallback(uri)
    }

    fun startSpotifyLogin(): Intent? {
        return authManager.buildAuthorizationIntent()
    }

    fun handleAuthCallback(uri: Uri) {
        viewModelScope.launch {
            authManager.handleCallback(uri)
        }
    }

    fun logout() {
        authManager.logout()
        _currentScreen.value = ScreenRoute.LOGIN
        screenBackStack.clear()
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            repository.refreshAllLibraryData()
        }
    }

    fun playTrack(track: SpotifyTrack, queue: List<SpotifyTrack>? = null) {
        playerManager.playTrack(track, queue)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun skipNext() {
        playerManager.skipNext()
    }

    fun skipPrevious() {
        playerManager.skipPrevious()
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun toggleLike(track: SpotifyTrack) {
        viewModelScope.launch {
            repository.toggleLikeTrack(track)
        }
    }

    fun isTrackLiked(trackId: String): Boolean {
        return repository.isTrackLiked(trackId)
    }

    fun addToQueue(track: SpotifyTrack) {
        playerManager.addToQueue(track)
    }

    fun playNext(track: SpotifyTrack) {
        playerManager.playNext(track)
    }

    fun removeFromQueue(trackId: String) {
        playerManager.removeFromQueue(trackId)
    }

    fun clearQueue() {
        playerManager.clearQueue()
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playerManager.toggleRepeat()
    }

    fun setVolume(vol: Float) {
        playerManager.setVolume(vol)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = SpotifySearchResponse()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(350L) // debounce
            _isSearching.value = true
            val results = repository.search(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun openInSpotifyApp(track: SpotifyTrack) {
        playerManager.openInSpotifyApp(track)
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
