package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.VibeSurface
import com.example.ui.viewmodel.ScreenRoute

@Composable
fun VibeBottomNav(
    currentRoute: ScreenRoute,
    onNavigate: (ScreenRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("vibe_bottom_nav"),
        containerColor = VibeSurface,
        tonalElevation = 8.dp
    ) {
        // Home
        NavigationBarItem(
            selected = currentRoute == ScreenRoute.HOME,
            onClick = { onNavigate(ScreenRoute.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == ScreenRoute.HOME) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = SpotifyGreen,
                indicatorColor = SpotifyGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Search
        NavigationBarItem(
            selected = currentRoute == ScreenRoute.SEARCH,
            onClick = { onNavigate(ScreenRoute.SEARCH) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == ScreenRoute.SEARCH) Icons.Default.Search else Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            },
            label = { Text("Search") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = SpotifyGreen,
                indicatorColor = SpotifyGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Library
        NavigationBarItem(
            selected = currentRoute == ScreenRoute.LIBRARY,
            onClick = { onNavigate(ScreenRoute.LIBRARY) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == ScreenRoute.LIBRARY) Icons.Default.LibraryMusic else Icons.Outlined.LibraryMusic,
                    contentDescription = "Library"
                )
            },
            label = { Text("Library") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = SpotifyGreen,
                indicatorColor = SpotifyGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Liked Songs
        NavigationBarItem(
            selected = currentRoute == ScreenRoute.LIKED,
            onClick = { onNavigate(ScreenRoute.LIKED) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == ScreenRoute.LIKED) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Liked"
                )
            },
            label = { Text("Liked") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = SpotifyGreen,
                indicatorColor = SpotifyGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Settings
        NavigationBarItem(
            selected = currentRoute == ScreenRoute.SETTINGS,
            onClick = { onNavigate(ScreenRoute.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == ScreenRoute.SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = SpotifyGreen,
                indicatorColor = SpotifyGreen,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
