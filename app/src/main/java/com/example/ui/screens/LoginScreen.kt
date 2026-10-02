package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthState
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    authState: AuthState,
    clientId: String,
    redirectUri: String,
    onLoginClick: () -> Unit,
    onSaveClientId: (String) -> Unit
) {
    var showConfigDialog by remember { mutableStateOf(false) }
    var inputClientId by remember { mutableStateOf(clientId) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        VibeBackground,
                        Color(0xFF060B12)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Logo and Glowing Pulse
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(24.dp, shape = CircleShape, spotColor = SpotifyGreen)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .border(2.dp, Brush.linearGradient(listOf(SpotifyGreen, NeonCyan)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "VibeFlow Logo",
                    modifier = Modifier.size(54.dp),
                    tint = SpotifyGreen
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Brand Name & Tagline
            Text(
                text = "VibeFlow",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Spotify Music Experience, Elevated.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Card highlight with value propositions
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VibeCard.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeatureItem(
                        icon = Icons.Default.Sync,
                        title = "Instant Spotify Sync",
                        subtitle = "Access all your playlists, liked tracks, top artists & history immediately."
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    FeatureItem(
                        icon = Icons.Default.MusicNote,
                        title = "Seamless In-App Player",
                        subtitle = "High-fidelity audio playback, continuous queue & Spotify Connect remote."
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    FeatureItem(
                        icon = Icons.Default.Security,
                        title = "Official Spotify OAuth",
                        subtitle = "Your credentials stay safe on Spotify. Zero passwords shared with VibeFlow."
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Error Message Banner if any
            if (authState is AuthState.Error) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1824)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = HeartRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = authState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFB4AB)
                        )
                    }
                }
            }

            // PRIMARY BUTTON: "Continue with Spotify"
            Button(
                onClick = onLoginClick,
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpotifyGreen,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("spotify_login_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continue with Spotify",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Configure Client ID Button
            TextButton(
                onClick = { showConfigDialog = true },
                modifier = Modifier.testTag("configure_client_id_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (clientId.isBlank()) "Setup Spotify Client ID" else "Client ID Configured",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Spotify Client ID Config Dialog
        if (showConfigDialog) {
            AlertDialog(
                onDismissRequest = { showConfigDialog = false },
                title = {
                    Text(
                        text = "Spotify Developer App Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column {
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                        var copiedNotice by remember { mutableStateOf<String?>(null) }

                        Text(
                            text = "To log into your real Spotify account, add this Redirect URI in your Spotify Developer Dashboard under App Settings > Redirect URIs:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Deep Link URI
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Android Deep Link URI:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("vibeflow://callback", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SpotifyGreen)
                                }
                                IconButton(onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("vibeflow://callback"))
                                    copiedNotice = "Copied vibeflow://callback"
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy URI", tint = SpotifyGreen, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        if (copiedNotice != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(copiedNotice ?: "", style = MaterialTheme.typography.labelSmall, color = SpotifyGreen)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = inputClientId,
                            onValueChange = { inputClientId = it },
                            label = { Text("Spotify Client ID") },
                            placeholder = { Text("Paste your 32-character Client ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onSaveClientId(inputClientId)
                            showConfigDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen, contentColor = Color.Black)
                    ) {
                        Text("Save & Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfigDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = VibeCard
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SpotifyGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SpotifyGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
