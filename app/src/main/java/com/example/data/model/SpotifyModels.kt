package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpotifyUser(
    val id: String,
    @Json(name = "display_name") val displayName: String?,
    val email: String? = null,
    val images: List<SpotifyImage> = emptyList(),
    val product: String? = null,
    val country: String? = null,
    val followers: SpotifyFollowers? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyFollowers(
    val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class SpotifyImage(
    val url: String,
    val height: Int? = null,
    val width: Int? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyArtistRef(
    val id: String = "",
    val name: String = "",
    val uri: String? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyAlbumRef(
    val id: String = "",
    val name: String = "",
    val images: List<SpotifyImage> = emptyList(),
    @Json(name = "release_date") val releaseDate: String? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyTrack(
    val id: String,
    val name: String,
    val uri: String = "",
    @Json(name = "duration_ms") val durationMs: Long = 0L,
    @Json(name = "preview_url") val previewUrl: String? = null,
    val artists: List<SpotifyArtistRef> = emptyList(),
    val album: SpotifyAlbumRef? = null,
    val explicit: Boolean = false,
    @Json(name = "is_playable") val isPlayable: Boolean? = true
) {
    val artistNames: String
        get() = artists.joinToString(", ") { it.name }.ifEmpty { "Unknown Artist" }

    val imageUrl: String?
        get() = album?.images?.firstOrNull()?.url

    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

@JsonClass(generateAdapter = true)
data class SpotifyUserRef(
    val id: String = "",
    @Json(name = "display_name") val displayName: String? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyPlaylist(
    val id: String,
    val name: String,
    val description: String? = null,
    val images: List<SpotifyImage> = emptyList(),
    val owner: SpotifyUserRef? = null,
    val tracks: SpotifyPlaylistTracksRef? = null,
    val public: Boolean? = true,
    val collaborative: Boolean = false
) {
    val imageUrl: String?
        get() = images.firstOrNull()?.url

    val trackCount: Int
        get() = tracks?.total ?: 0
}

@JsonClass(generateAdapter = true)
data class SpotifyPlaylistTracksRef(
    val href: String? = null,
    val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class SpotifyPlaylistTrackItem(
    @Json(name = "added_at") val addedAt: String? = null,
    val track: SpotifyTrack? = null
)

@JsonClass(generateAdapter = true)
data class SpotifySavedTrackItem(
    @Json(name = "added_at") val addedAt: String? = null,
    val track: SpotifyTrack
)

@JsonClass(generateAdapter = true)
data class SpotifyRecentlyPlayedItem(
    val track: SpotifyTrack,
    @Json(name = "played_at") val playedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyArtist(
    val id: String,
    val name: String,
    val images: List<SpotifyImage> = emptyList(),
    val genres: List<String> = emptyList(),
    val followers: SpotifyFollowers? = null,
    val popularity: Int? = null
) {
    val imageUrl: String?
        get() = images.firstOrNull()?.url
}

@JsonClass(generateAdapter = true)
data class SpotifyAlbum(
    val id: String,
    val name: String,
    val images: List<SpotifyImage> = emptyList(),
    val artists: List<SpotifyArtistRef> = emptyList(),
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "total_tracks") val totalTracks: Int = 0
) {
    val imageUrl: String?
        get() = images.firstOrNull()?.url

    val artistNames: String
        get() = artists.joinToString(", ") { it.name }.ifEmpty { "Unknown Artist" }
}

@JsonClass(generateAdapter = true)
data class SpotifyPaging<T>(
    val href: String? = null,
    val items: List<T> = emptyList(),
    val limit: Int = 20,
    val offset: Int = 0,
    val total: Int = 0,
    val next: String? = null,
    val previous: String? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyRecentlyPlayedResponse(
    val items: List<SpotifyRecentlyPlayedItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SpotifyFollowedArtistsResponse(
    val artists: SpotifyPaging<SpotifyArtist>
)

@JsonClass(generateAdapter = true)
data class SpotifySearchResponse(
    val tracks: SpotifyPaging<SpotifyTrack>? = null,
    val playlists: SpotifyPaging<SpotifyPlaylist>? = null,
    val artists: SpotifyPaging<SpotifyArtist>? = null,
    val albums: SpotifyPaging<SpotifyAlbum>? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyAuthTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String = "Bearer",
    val scope: String? = null,
    @Json(name = "expires_in") val expiresIn: Long = 3600L,
    @Json(name = "refresh_token") val refreshToken: String? = null
)

data class SpotifyDevice(
    val id: String,
    val name: String,
    val type: String,
    val isActive: Boolean = false,
    val volumePercent: Int = 100
)

data class SpotifyPlaybackState(
    val track: SpotifyTrack? = null,
    val isPlaying: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val isLiked: Boolean = false,
    val activeDeviceName: String? = null
)
