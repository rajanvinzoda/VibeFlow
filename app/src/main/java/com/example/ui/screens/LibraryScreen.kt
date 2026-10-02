package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    selectedTab: Int,
    playlists: List<SpotifyPlaylist>,
    savedTracks: List<SpotifyTrack>,
    artists: List<SpotifyArtist>,
    currentTrack: SpotifyTrack?,
    isPlaying: Boolean,
    onTabSelected: (Int) -> Unit,
    onPlaylistClick: (SpotifyPlaylist) -> Unit,
    onTrackClick: (SpotifyTrack) -> Unit,
    onArtistClick: (SpotifyArtist) -> Unit,
    onLikeToggle: (SpotifyTrack) -> Unit,
    onAddToQueue: (SpotifyTrack) -> Unit,
    onPlayNext: (SpotifyTrack) -> Unit,
    onOpenInSpotify: (SpotifyTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabTitles = listOf("Playlists", "Songs", "Artists", "Albums")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        // Library Header
        Text(
            text = "Your Library",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 12.dp)
        )

        // Scrollable / primary Tab Row
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = SpotifyGreen,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selectedTab == index) SpotifyGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Playlists Grid
                    if (playlists.isEmpty()) {
                        EmptyLibraryState(message = "No Spotify playlists found.")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(playlists) { playlist ->
                                PlaylistCard(
                                    playlist = playlist,
                                    onClick = { onPlaylistClick(playlist) },
                                    onPlayClick = { onPlaylistClick(playlist) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Saved / Liked Songs List
                    if (savedTracks.isEmpty()) {
                        EmptyLibraryState(message = "No liked songs yet.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(savedTracks) { track ->
                                TrackItemRow(
                                    track = track,
                                    isCurrentTrack = currentTrack?.id == track.id,
                                    isPlaying = isPlaying,
                                    isLiked = true,
                                    onTrackClick = { onTrackClick(track) },
                                    onLikeClick = { onLikeToggle(track) },
                                    onAddToQueue = { onAddToQueue(track) },
                                    onPlayNext = { onPlayNext(track) },
                                    onOpenInSpotify = { onOpenInSpotify(track) }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Artists List
                    if (artists.isEmpty()) {
                        EmptyLibraryState(message = "No followed artists.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(artists) { artist ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onArtistClick(artist) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(VibeSurfaceVariant)
                                    ) {
                                        val img = artist.imageUrl
                                        if (!img.isNullOrBlank()) {
                                            AsyncImage(
                                                model = img,
                                                contentDescription = artist.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = SpotifyGreen,
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .align(Alignment.Center)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = artist.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = artist.genres.take(2).joinToString(" • ").ifEmpty { "Artist" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Albums derived from saved tracks
                    val albums = savedTracks.mapNotNull { it.album }.distinctBy { it.id }
                    if (albums.isEmpty()) {
                        EmptyLibraryState(message = "No saved albums.")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(albums) { albumRef ->
                                AlbumCard(
                                    album = SpotifyAlbum(
                                        id = albumRef.id,
                                        name = albumRef.name,
                                        images = albumRef.images,
                                        artists = emptyList(),
                                        releaseDate = albumRef.releaseDate
                                    ),
                                    onClick = { /* Open album tracks */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
