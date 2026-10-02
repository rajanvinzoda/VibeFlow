package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.ArtistCard
import com.example.ui.components.PlaylistCard
import com.example.ui.components.TrackItemRow
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.VibeSurfaceVariant

@Composable
fun SearchScreen(
    searchQuery: String,
    searchResults: SpotifySearchResponse,
    isSearching: Boolean,
    currentTrack: SpotifyTrack?,
    isPlaying: Boolean,
    savedTracks: List<SpotifyTrack>,
    onQueryChange: (String) -> Unit,
    onTrackClick: (SpotifyTrack) -> Unit,
    onPlaylistClick: (SpotifyPlaylist) -> Unit,
    onArtistClick: (SpotifyArtist) -> Unit,
    onLikeToggle: (SpotifyTrack) -> Unit,
    onAddToQueue: (SpotifyTrack) -> Unit,
    onPlayNext: (SpotifyTrack) -> Unit,
    onOpenInSpotify: (SpotifyTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Songs", "Artists", "Playlists")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("What do you want to listen to?", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = VibeSurfaceVariant,
                unfocusedContainerColor = VibeSurfaceVariant,
                focusedBorderColor = SpotifyGreen,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("search_text_field")
        )

        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotifyGreen,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SpotifyGreen)
            }
        } else if (searchQuery.isBlank()) {
            // Initial Explore State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Search Spotify",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Find songs, artists, playlists, and albums.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Results List
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                val tracks = searchResults.tracks?.items.orEmpty()
                val artists = searchResults.artists?.items.orEmpty()
                val playlists = searchResults.playlists?.items.orEmpty()

                // Artists horizontal section
                if ((selectedFilter == "All" || selectedFilter == "Artists") && artists.isNotEmpty()) {
                    item {
                        Text(
                            text = "Artists",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(artists) { artist ->
                                ArtistCard(artist = artist, onClick = { onArtistClick(artist) })
                            }
                        }
                    }
                }

                // Playlists section
                if ((selectedFilter == "All" || selectedFilter == "Playlists") && playlists.isNotEmpty()) {
                    item {
                        Text(
                            text = "Playlists",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
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

                // Songs section
                if ((selectedFilter == "All" || selectedFilter == "Songs") && tracks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Songs",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    items(tracks) { track ->
                        TrackItemRow(
                            track = track,
                            isCurrentTrack = currentTrack?.id == track.id,
                            isPlaying = isPlaying,
                            isLiked = savedTracks.any { it.id == track.id },
                            onTrackClick = { onTrackClick(track) },
                            onLikeClick = { onLikeToggle(track) },
                            onAddToQueue = { onAddToQueue(track) },
                            onPlayNext = { onPlayNext(track) },
                            onOpenInSpotify = { onOpenInSpotify(track) },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                if (tracks.isEmpty() && artists.isEmpty() && playlists.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results found for '$searchQuery'",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
