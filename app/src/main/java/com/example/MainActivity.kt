package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BottomPlayerBar
import com.example.ui.components.NowPlayingSheet
import com.example.ui.components.SpotifyOAuthWebView
import com.example.ui.components.VibeBottomNav
import com.example.ui.screens.*
import com.example.ui.theme.VibeBackground
import com.example.ui.theme.VibeFlowTheme
import com.example.ui.viewmodel.ScreenRoute
import com.example.ui.viewmodel.VibeFlowViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VibeFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleOAuthIntent(intent)

        setContent {
            VibeFlowTheme {
                VibeFlowApp(
                    viewModel = viewModel,
                    onLaunchAuthIntent = { authIntent ->
                        try {
                            startActivity(authIntent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                this,
                                "No web browser found to open Spotify login: ${e.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val data = intent?.data
        if (data != null && (
            (data.scheme == "vibeflow" && data.host == "callback") ||
            (data.path?.startsWith("/callback") == true)
        )) {
            viewModel.handleAuthCallback(data)
        }
    }
}

@Composable
fun VibeFlowApp(
    viewModel: VibeFlowViewModel,
    onLaunchAuthIntent: (Intent) -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val authState by viewModel.authManager.authState.collectAsStateWithLifecycle()
    val currentUser by viewModel.repository.currentUser.collectAsStateWithLifecycle()
    val playlists by viewModel.repository.userPlaylists.collectAsStateWithLifecycle()
    val savedTracks by viewModel.repository.savedTracks.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.repository.recentlyPlayed.collectAsStateWithLifecycle()
    val topTracks by viewModel.repository.topTracks.collectAsStateWithLifecycle()
    val topArtists by viewModel.repository.topArtists.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.repository.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.repository.errorMessage.collectAsStateWithLifecycle()

    val currentTrack by viewModel.playerManager.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.playerManager.isPlaying.collectAsStateWithLifecycle()
    val progressMs by viewModel.playerManager.progressMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.playerManager.durationMs.collectAsStateWithLifecycle()
    val queue by viewModel.playerManager.queue.collectAsStateWithLifecycle()
    val isShuffle by viewModel.playerManager.isShuffle.collectAsStateWithLifecycle()
    val isRepeat by viewModel.playerManager.isRepeat.collectAsStateWithLifecycle()
    val volume by viewModel.playerManager.volume.collectAsStateWithLifecycle()
    val statusNotice by viewModel.playerManager.statusNotice.collectAsStateWithLifecycle()

    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val playlistTracks by viewModel.playlistTracks.collectAsStateWithLifecycle()
    val isLoadingPlaylistTracks by viewModel.isLoadingPlaylistTracks.collectAsStateWithLifecycle()

    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()
    val selectedLibraryTab by viewModel.selectedLibraryTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val authUrlForWebView by viewModel.authUrlForWebView.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusNotice) {
        if (!statusNotice.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = statusNotice ?: "",
                duration = SnackbarDuration.Short
            )
            viewModel.playerManager.clearStatusNotice()
        }
    }

    // Handle Android system back gesture / button
    BackHandler {
        val handled = viewModel.navigateBack()
        if (!handled && currentScreen != ScreenRoute.HOME && currentScreen != ScreenRoute.LOGIN) {
            viewModel.navigateTo(ScreenRoute.HOME)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(VibeBackground),
        containerColor = VibeBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentScreen != ScreenRoute.LOGIN) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Persistent Bottom Mini Player
                    BottomPlayerBar(
                        track = currentTrack,
                        isPlaying = isPlaying,
                        progressMs = progressMs,
                        durationMs = durationMs,
                        isLiked = currentTrack?.let { viewModel.isTrackLiked(it.id) } ?: false,
                        onBarClick = { viewModel.setNowPlayingExpanded(true) },
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onNextClick = { viewModel.skipNext() },
                        onLikeClick = { currentTrack?.let { viewModel.toggleLike(it) } },
                        onQueueClick = { viewModel.navigateTo(ScreenRoute.QUEUE) }
                    )

                    // Navigation Bar
                    VibeBottomNav(
                        currentRoute = currentScreen,
                        onNavigate = { route -> viewModel.navigateTo(route) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (currentScreen != ScreenRoute.LOGIN) innerPadding.calculateBottomPadding() else 0.dp
                )
                .statusBarsPadding()
        ) {
            when (currentScreen) {
                ScreenRoute.LOGIN -> {
                    LoginScreen(
                        authState = authState,
                        clientId = viewModel.authManager.getClientId(),
                        redirectUri = viewModel.authManager.getRedirectUri(),
                        onLoginClick = {
                            viewModel.openInAppLogin()
                        },
                        onSaveClientId = { id -> viewModel.authManager.saveCustomClientId(id) }
                    )
                }

                ScreenRoute.HOME -> {
                    HomeScreen(
                        user = currentUser,
                        playlists = playlists,
                        savedTracks = savedTracks,
                        recentlyPlayed = recentlyPlayed,
                        topTracks = topTracks,
                        topArtists = topArtists,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        isRefreshing = isRefreshing,
                        errorMessage = errorMessage,
                        onLoginClick = { viewModel.openInAppLogin() },
                        onRefresh = { viewModel.refreshLibrary() },
                        onTrackClick = { track -> viewModel.playTrack(track, savedTracks) },
                        onPlaylistClick = { playlist -> viewModel.openPlaylist(playlist) },
                        onArtistClick = { /* View artist */ },
                        onLikedSongsClick = { viewModel.navigateTo(ScreenRoute.LIKED) },
                        onLikeToggle = { track -> viewModel.toggleLike(track) },
                        onAddToQueue = { track -> viewModel.addToQueue(track) },
                        onPlayNext = { track -> viewModel.playNext(track) },
                        onOpenInSpotify = { track -> viewModel.openInSpotifyApp(track) }
                    )
                }

                ScreenRoute.LIBRARY -> {
                    LibraryScreen(
                        selectedTab = selectedLibraryTab,
                        playlists = playlists,
                        savedTracks = savedTracks,
                        artists = topArtists,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onTabSelected = { tab -> viewModel.setLibraryTab(tab) },
                        onPlaylistClick = { playlist -> viewModel.openPlaylist(playlist) },
                        onTrackClick = { track -> viewModel.playTrack(track, savedTracks) },
                        onArtistClick = { /* View artist */ },
                        onLikeToggle = { track -> viewModel.toggleLike(track) },
                        onAddToQueue = { track -> viewModel.addToQueue(track) },
                        onPlayNext = { track -> viewModel.playNext(track) },
                        onOpenInSpotify = { track -> viewModel.openInSpotifyApp(track) }
                    )
                }

                ScreenRoute.LIKED -> {
                    LikedSongsScreen(
                        savedTracks = savedTracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onTrackClick = { track -> viewModel.playTrack(track, savedTracks) },
                        onPlayAll = {
                            savedTracks.firstOrNull()?.let {
                                viewModel.playTrack(it, savedTracks)
                            }
                        },
                        onShuffleAll = {
                            if (savedTracks.isNotEmpty()) {
                                val shuffled = savedTracks.shuffled()
                                viewModel.playTrack(shuffled.first(), shuffled)
                            }
                        },
                        onAddAllToQueue = {
                            savedTracks.forEach { viewModel.addToQueue(it) }
                        },
                        onLikeToggle = { track -> viewModel.toggleLike(track) },
                        onAddToQueue = { track -> viewModel.addToQueue(track) },
                        onPlayNext = { track -> viewModel.playNext(track) },
                        onOpenInSpotify = { track -> viewModel.openInSpotifyApp(track) }
                    )
                }

                ScreenRoute.PLAYLIST_DETAIL -> {
                    PlaylistDetailScreen(
                        playlist = selectedPlaylist,
                        tracks = playlistTracks,
                        isLoading = isLoadingPlaylistTracks,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        onBackClick = { viewModel.navigateBack() },
                        onTrackClick = { track -> viewModel.playTrack(track, playlistTracks) },
                        onPlayAll = {
                            playlistTracks.firstOrNull()?.let {
                                viewModel.playTrack(it, playlistTracks)
                            }
                        },
                        onShuffleAll = {
                            if (playlistTracks.isNotEmpty()) {
                                val shuffled = playlistTracks.shuffled()
                                viewModel.playTrack(shuffled.first(), shuffled)
                            }
                        },
                        onLikeToggle = { track -> viewModel.toggleLike(track) },
                        onAddToQueue = { track -> viewModel.addToQueue(track) },
                        onPlayNext = { track -> viewModel.playNext(track) },
                        onOpenInSpotify = { track -> viewModel.openInSpotifyApp(track) }
                    )
                }

                ScreenRoute.SEARCH -> {
                    SearchScreen(
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        isSearching = isSearching,
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        savedTracks = savedTracks,
                        onQueryChange = { query -> viewModel.onSearchQueryChanged(query) },
                        onTrackClick = { track -> viewModel.playTrack(track) },
                        onPlaylistClick = { playlist -> viewModel.openPlaylist(playlist) },
                        onArtistClick = { /* View artist */ },
                        onLikeToggle = { track -> viewModel.toggleLike(track) },
                        onAddToQueue = { track -> viewModel.addToQueue(track) },
                        onPlayNext = { track -> viewModel.playNext(track) },
                        onOpenInSpotify = { track -> viewModel.openInSpotifyApp(track) }
                    )
                }

                ScreenRoute.QUEUE -> {
                    QueueScreen(
                        currentTrack = currentTrack,
                        queue = queue,
                        isPlaying = isPlaying,
                        onBackClick = { viewModel.navigateBack() },
                        onTrackClick = { track -> viewModel.playTrack(track) },
                        onRemoveFromQueue = { trackId -> viewModel.removeFromQueue(trackId) },
                        onClearQueue = { viewModel.clearQueue() }
                    )
                }

                ScreenRoute.SETTINGS -> {
                    SettingsScreen(
                        user = currentUser,
                        clientId = viewModel.authManager.getClientId(),
                        redirectUri = viewModel.authManager.getRedirectUri(),
                        onSaveClientId = { id -> viewModel.authManager.saveCustomClientId(id) },
                        onRefreshLibrary = { viewModel.refreshLibrary() },
                        onLogout = { viewModel.logout() }
                    )
                }
            }
        }
    }

    // In-App Spotify OAuth Dialog (handles phone/OTP and seamless callback interception)
    if (authUrlForWebView != null) {
        SpotifyOAuthWebView(
            authUrl = authUrlForWebView ?: "",
            onRedirectUrlIntercepted = { uri ->
                viewModel.onAuthCodeReceivedFromWebView(uri)
            },
            onDismiss = {
                viewModel.dismissInAppLogin()
            }
        )
    }

    // Modal Bottom Sheet: Full Expanded Now Playing
    if (isNowPlayingExpanded) {
        NowPlayingSheet(
            track = currentTrack,
            isPlaying = isPlaying,
            progressMs = progressMs,
            durationMs = durationMs,
            isLiked = currentTrack?.let { viewModel.isTrackLiked(it.id) } ?: false,
            isShuffle = isShuffle,
            isRepeat = isRepeat,
            volume = volume,
            statusNotice = statusNotice,
            onDismiss = { viewModel.setNowPlayingExpanded(false) },
            onPlayPause = { viewModel.togglePlayPause() },
            onNext = { viewModel.skipNext() },
            onPrevious = { viewModel.skipPrevious() },
            onSeek = { ms -> viewModel.seekTo(ms) },
            onLikeToggle = { currentTrack?.let { viewModel.toggleLike(it) } },
            onShuffleToggle = { viewModel.toggleShuffle() },
            onRepeatToggle = { viewModel.toggleRepeat() },
            onVolumeChange = { vol -> viewModel.setVolume(vol) },
            onQueueClick = {
                viewModel.setNowPlayingExpanded(false)
                viewModel.navigateTo(ScreenRoute.QUEUE)
            },
            onOpenInSpotify = { currentTrack?.let { viewModel.openInSpotifyApp(it) } }
        )
    }
}
