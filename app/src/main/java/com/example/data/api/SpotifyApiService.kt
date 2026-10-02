package com.example.data.api

import com.example.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SpotifyApiService {

    @GET("v1/me")
    suspend fun getCurrentUser(): SpotifyUser

    @GET("v1/me/playlists")
    suspend fun getCurrentUserPlaylists(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): SpotifyPaging<SpotifyPlaylist>

    @GET("v1/playlists/{playlist_id}")
    suspend fun getPlaylist(
        @Path("playlist_id") playlistId: String
    ): SpotifyPlaylist

    @GET("v1/playlists/{playlist_id}/tracks")
    suspend fun getPlaylistTracks(
        @Path("playlist_id") playlistId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): SpotifyPaging<SpotifyPlaylistTrackItem>

    @GET("v1/me/tracks")
    suspend fun getSavedTracks(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): SpotifyPaging<SpotifySavedTrackItem>

    @PUT("v1/me/tracks")
    suspend fun saveTrack(
        @Query("ids") ids: String
    ): Response<Unit>

    @DELETE("v1/me/tracks")
    suspend fun removeSavedTrack(
        @Query("ids") ids: String
    ): Response<Unit>

    @GET("v1/me/tracks/contains")
    suspend fun checkSavedTracks(
        @Query("ids") ids: String
    ): List<Boolean>

    @GET("v1/me/player/recently-played")
    suspend fun getRecentlyPlayed(
        @Query("limit") limit: Int = 30
    ): SpotifyRecentlyPlayedResponse

    @GET("v1/me/top/tracks")
    suspend fun getTopTracks(
        @Query("limit") limit: Int = 30,
        @Query("time_range") timeRange: String = "medium_term"
    ): SpotifyPaging<SpotifyTrack>

    @GET("v1/me/top/artists")
    suspend fun getTopArtists(
        @Query("limit") limit: Int = 30,
        @Query("time_range") timeRange: String = "medium_term"
    ): SpotifyPaging<SpotifyArtist>

    @GET("v1/me/following")
    suspend fun getFollowedArtists(
        @Query("type") type: String = "artist",
        @Query("limit") limit: Int = 30
    ): SpotifyFollowedArtistsResponse

    @GET("v1/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "track,playlist,artist,album",
        @Query("limit") limit: Int = 20
    ): SpotifySearchResponse

    @GET("v1/tracks/{id}")
    suspend fun getTrack(
        @Path("id") id: String
    ): SpotifyTrack

    @GET("v1/albums/{id}")
    suspend fun getAlbum(
        @Path("id") id: String
    ): SpotifyAlbum

    @GET("v1/artists/{id}")
    suspend fun getArtist(
        @Path("id") id: String
    ): SpotifyArtist

    @POST("v1/me/player/queue")
    suspend fun addToQueue(
        @Query("uri") uri: String
    ): Response<Unit>

    @PUT("v1/me/player/play")
    suspend fun play(
        @Body body: Map<String, Any> = emptyMap()
    ): Response<Unit>

    @PUT("v1/me/player/pause")
    suspend fun pause(): Response<Unit>

    @POST("v1/me/player/next")
    suspend fun next(): Response<Unit>

    @POST("v1/me/player/previous")
    suspend fun previous(): Response<Unit>

    @PUT("v1/me/player/seek")
    suspend fun seek(
        @Query("position_ms") positionMs: Long
    ): Response<Unit>

    @PUT("v1/me/player/shuffle")
    suspend fun setShuffle(
        @Query("state") state: Boolean
    ): Response<Unit>

    @PUT("v1/me/player/repeat")
    suspend fun setRepeat(
        @Query("state") state: String
    ): Response<Unit>
}
