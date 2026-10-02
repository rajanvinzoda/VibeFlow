package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SpotifyUser
import com.example.ui.theme.HeartRed
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.VibeCard
import com.example.ui.theme.VibeSurfaceVariant

@Composable
fun SettingsScreen(
    user: SpotifyUser?,
    clientId: String,
    redirectUri: String,
    onSaveClientId: (String) -> Unit,
    onRefreshLibrary: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputClientId by remember { mutableStateOf(clientId) }
    var showClientIdSaved by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 120.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = VibeCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(VibeSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = user?.images?.firstOrNull()?.url
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user?.displayName ?: "Spotify User",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = user?.email ?: "Connected via Spotify OAuth",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = SpotifyGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = (user?.product?.uppercase() ?: "PREMIUM"),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SpotifyGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Spotify Developer Integration Section
        Text(
            text = "Spotify Developer Integration",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VibeCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Configure your official Spotify Developer Client ID for live account synchronization and playback.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    var uriCopied by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Registered Redirect URI:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = redirectUri,
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyGreen
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(redirectUri))
                            uriCopied = true
                        }
                    ) {
                        Icon(
                            imageVector = if (uriCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy URI",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputClientId,
                    onValueChange = {
                        inputClientId = it
                        showClientIdSaved = false
                    },
                    label = { Text("Spotify Client ID") },
                    placeholder = { Text("Enter Client ID from Spotify Dashboard") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VibeSurfaceVariant,
                        unfocusedContainerColor = VibeSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onSaveClientId(inputClientId)
                        showClientIdSaved = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpotifyGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Client ID")
                }

                if (showClientIdSaved) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Client ID saved! Click Login to authenticate with Spotify.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotifyGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        Text(
            text = "Actions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VibeCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                ListItem(
                    headlineContent = { Text("Resync Spotify Library") },
                    supportingContent = { Text("Reload playlists, liked tracks and recent history") },
                    leadingContent = {
                        Icon(Icons.Outlined.Sync, contentDescription = null, tint = SpotifyGreen)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Button(
                    onClick = onRefreshLibrary,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibeSurfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Sync Now")
                }

                HorizontalDivider(color = VibeSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = { Text("Log Out", color = HeartRed) },
                    supportingContent = { Text("Disconnect Spotify session and clear tokens") },
                    leadingContent = {
                        Icon(Icons.Outlined.Logout, contentDescription = null, tint = HeartRed)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                OutlinedButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HeartRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Log Out")
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // About VibeFlow
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "VibeFlow v1.0",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Independent client powered by official Spotify APIs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
