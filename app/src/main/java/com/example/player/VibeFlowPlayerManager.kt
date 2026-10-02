package com.example.player

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.api.SpotifyApiService
import com.example.data.auth.SpotifyAuthManager
import com.example.data.model.SpotifyTrack
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

enum class PlaybackType {
    IN_APP_PREVIEW,
    SPOTIFY_CONNECT
}

class VibeFlowPlayerManager(
    private val context: Context,
    private val authManager: SpotifyAuthManager,
    private val apiService: () -> SpotifyApiService?
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _currentTrack = MutableStateFlow<SpotifyTrack?>(null)
    val currentTrack: StateFlow<SpotifyTrack?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progressMs = MutableStateFlow(0L)
    val progressMs: StateFlow<Long> = _progressMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<SpotifyTrack>>(emptyList())
    val queue: StateFlow<List<SpotifyTrack>> = _queue.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _statusNotice = MutableStateFlow<String?>(null)
    val statusNotice: StateFlow<String?> = _statusNotice.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    init {
        // Preload an initial track from demo if available so player is ready
        _durationMs.value = 30000L
    }

    fun playTrack(track: SpotifyTrack, newQueue: List<SpotifyTrack>? = null) {
        _currentTrack.value = track
        if (newQueue != null) {
            _queue.value = newQueue.filter { it.id != track.id }
        }

        val previewUrl = track.previewUrl
        if (!previewUrl.isNullOrBlank()) {
            playAudioUrl(previewUrl, track.durationMs)
        } else {
            // No direct 30s preview URL available for this specific track
            // Attempt Spotify Connect Web API playback
            triggerSpotifyConnectPlayback(track)
            _durationMs.value = if (track.durationMs > 0) track.durationMs else 180000L
            _progressMs.value = 0L
            _isPlaying.value = true
            startProgressTicker()
            _statusNotice.value = "Playing '${track.name}'. Use 'Open in Spotify' for full playback."
        }
    }

    private fun playAudioUrl(url: String, totalDurationMs: Long) {
        stopMediaPlayer()

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setVolume(_volume.value, _volume.value)
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    _durationMs.value = if (mp.duration > 0) mp.duration.toLong() else if (totalDurationMs > 0) totalDurationMs else 30000L
                    startProgressTicker()
                }
                setOnCompletionListener {
                    onTrackCompleted()
                }
                setOnErrorListener { _, what, extra ->
                    _statusNotice.value = "Playback note: Audio stream ended ($what:$extra)"
                    _isPlaying.value = false
                    stopProgressTicker()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: IOException) {
            _statusNotice.value = "Unable to stream audio: ${e.message}"
            _isPlaying.value = false
        }
    }

    private fun triggerSpotifyConnectPlayback(track: SpotifyTrack) {
        scope.launch(Dispatchers.IO) {
            try {
                val service = apiService()
                service?.play(
                    mapOf("uris" to listOf("spotify:track:${track.id}"))
                )
            } catch (e: Exception) {
                // If Spotify Connect is not active, user can open track in Spotify
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer
        if (player != null) {
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                stopProgressTicker()
            } else {
                player.start()
                _isPlaying.value = true
                startProgressTicker()
            }
        } else if (_currentTrack.value != null) {
            _isPlaying.value = !_isPlaying.value
            if (_isPlaying.value) {
                startProgressTicker()
            } else {
                stopProgressTicker()
            }
        }
    }

    fun skipNext() {
        val q = _queue.value
        if (q.isNotEmpty()) {
            val nextTrack = if (_isShuffle.value) {
                q.random()
            } else {
                q.first()
            }
            val remainingQueue = q.filter { it.id != nextTrack.id }
            playTrack(nextTrack, remainingQueue)
        } else if (_isRepeat.value && _currentTrack.value != null) {
            seekTo(0)
            if (!_isPlaying.value) togglePlayPause()
        } else {
            _isPlaying.value = false
            stopProgressTicker()
        }
    }

    fun skipPrevious() {
        if (_progressMs.value > 3000L) {
            seekTo(0L)
        } else {
            // Seek to start
            seekTo(0L)
        }
    }

    fun seekTo(positionMs: Long) {
        _progressMs.value = positionMs
        mediaPlayer?.let {
            if (it.duration > 0) {
                it.seekTo(positionMs.toInt())
            }
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        mediaPlayer?.setVolume(clamped, clamped)
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    fun addToQueue(track: SpotifyTrack) {
        _queue.value = _queue.value + track
        _statusNotice.value = "Added '${track.name}' to queue"
    }

    fun playNext(track: SpotifyTrack) {
        _queue.value = listOf(track) + _queue.value.filter { it.id != track.id }
        _statusNotice.value = "Will play '${track.name}' next"
    }

    fun removeFromQueue(trackId: String) {
        _queue.value = _queue.value.filter { it.id != trackId }
    }

    fun clearQueue() {
        _queue.value = emptyList()
    }

    fun clearStatusNotice() {
        _statusNotice.value = null
    }

    fun openInSpotifyApp(track: SpotifyTrack) {
        try {
            val uri = Uri.parse("spotify:track:${track.id}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser
            val webUri = Uri.parse("https://open.spotify.com/track/${track.id}")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }

    private fun onTrackCompleted() {
        if (_isRepeat.value) {
            seekTo(0L)
            mediaPlayer?.start()
        } else {
            skipNext()
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                delay(300L)
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        _progressMs.value = it.currentPosition.toLong()
                        if (it.duration > 0) {
                            _durationMs.value = it.duration.toLong()
                        }
                    }
                } ?: run {
                    val next = _progressMs.value + 300L
                    if (next >= _durationMs.value && _durationMs.value > 0) {
                        onTrackCompleted()
                    } else {
                        _progressMs.value = next
                    }
                }
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun stopMediaPlayer() {
        stopProgressTicker()
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
        mediaPlayer = null
    }

    fun release() {
        stopMediaPlayer()
        scope.cancel()
    }
}
