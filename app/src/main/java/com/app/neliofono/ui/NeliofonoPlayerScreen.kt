package com.app.neliofono.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.neliofono.model.PlayerAction
import com.app.neliofono.model.TrackInfo
import com.app.neliofono.ui.components.KeyGuideBar
import com.app.neliofono.ui.components.RecordDiscPlaceholder
import com.app.neliofono.ui.theme.GoldAccent
import com.app.neliofono.ui.theme.GoldAccentDark
import com.app.neliofono.ui.theme.TextMuted
import com.app.neliofono.ui.theme.TextPrimary
import com.app.neliofono.ui.theme.TextSecondary
import com.app.neliofono.ui.theme.VinylBlack
import com.app.neliofono.ui.theme.VinylBorder
import com.app.neliofono.ui.theme.VinylCardBg
import com.app.neliofono.ui.theme.VinylDarkGray
import com.app.neliofono.viewmodel.PlayerUiState
import com.app.neliofono.viewmodel.PlayerViewModel
import java.util.Locale

@Composable
fun NeliofonoPlayerScreen(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = VinylBlack
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Check aspect ratio (RG Rotate 1:1 or 4:3 / Landscape vs Portrait)
            val aspectRatio = maxWidth / maxHeight
            val isLandscapeWide = aspectRatio > 1.15f

            if (isLandscapeWide) {
                LandscapeLayout(
                    uiState = uiState,
                    onAction = { viewModel.handleAction(it) },
                    onSeek = { viewModel.seekTo(it) }
                )
            } else {
                // Square (1:1 like RG Rotate 720x720) or Portrait
                SquarePortraitLayout(
                    uiState = uiState,
                    onAction = { viewModel.handleAction(it) },
                    onSeek = { viewModel.seekTo(it) }
                )
            }

            // Playlist Overlay
            AnimatedVisibility(
                visible = uiState.isPlaylistViewOpen,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                PlaylistModalSheet(
                    uiState = uiState,
                    onClose = { viewModel.toggleViewMode() },
                    onSelectTrack = { idx ->
                        while (uiState.currentIndex != idx) {
                            viewModel.nextTrack()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SquarePortraitLayout(
    uiState: PlayerUiState,
    onAction: (PlayerAction) -> Unit,
    onSeek: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header
        AppHeader(onPlaylistClick = { onAction(PlayerAction.SwitchViewMode) })

        // Record Disc in Top Half
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            RecordDiscPlaceholder(
                isPlaying = uiState.isPlaying,
                modifier = Modifier
                    .fillMaxHeight(0.92f)
                    .aspectRatio(1f)
            )
        }

        // Bottom Controls Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrackMetaSection(track = uiState.currentTrack)

            Spacer(modifier = Modifier.height(4.dp))

            SeekBarSection(
                currentPosMs = uiState.currentPositionMs,
                totalDurationMs = uiState.currentTrack.durationMs,
                onSeek = onSeek
            )

            Spacer(modifier = Modifier.height(4.dp))

            PlaybackControlButtons(
                isPlaying = uiState.isPlaying,
                onAction = onAction
            )

            Spacer(modifier = Modifier.height(6.dp))

            KeyGuideBar(
                recentLogs = uiState.recentKeyLogs,
                latestKeyAction = uiState.latestKeyAction
            )
        }
    }
}

@Composable
private fun LandscapeLayout(
    uiState: PlayerUiState,
    onAction: (PlayerAction) -> Unit,
    onSeek: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Record Disc
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            RecordDiscPlaceholder(
                isPlaying = uiState.isPlaying,
                modifier = Modifier
                    .fillMaxHeight(0.92f)
                    .aspectRatio(1f)
            )
        }

        // Right Side: Info, Controls, Key Guide
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader(onPlaylistClick = { onAction(PlayerAction.SwitchViewMode) })

            TrackMetaSection(track = uiState.currentTrack)

            SeekBarSection(
                currentPosMs = uiState.currentPositionMs,
                totalDurationMs = uiState.currentTrack.durationMs,
                onSeek = onSeek
            )

            PlaybackControlButtons(
                isPlaying = uiState.isPlaying,
                onAction = onAction
            )

            KeyGuideBar(
                recentLogs = uiState.recentKeyLogs,
                latestKeyAction = uiState.latestKeyAction
            )
        }
    }
}

@Composable
fun AppHeader(
    onPlaylistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(GoldAccentDark)
                    .border(1.dp, GoldAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    tint = VinylBlack,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "NELIOFONO",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = GoldAccent,
                    fontSize = 13.sp
                )
            )
        }

        IconButton(
            onClick = onPlaylistClick,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = "Playlist",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TrackMetaSection(
    track: TrackInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = track.title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 14.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${track.artist} • ${track.album}",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SeekBarSection(
    currentPosMs: Long,
    totalDurationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalDurationMs > 0) {
        (currentPosMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = progress,
            onValueChange = { newProg ->
                val newPos = (newProg * totalDurationMs).toLong()
                onSeek(newPos)
            },
            colors = SliderDefaults.colors(
                thumbColor = GoldAccent,
                activeTrackColor = GoldAccent,
                inactiveTrackColor = VinylBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(currentPosMs),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 9.sp
                )
            )
            Text(
                text = formatDuration(totalDurationMs),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
fun PlaybackControlButtons(
    isPlaying: Boolean,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onAction(PlayerAction.PreviousTrack) },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(VinylCardBg)
                .border(1.dp, VinylBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous Track",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GoldAccent,
                            GoldAccentDark
                        )
                    )
                )
                .clickable { onAction(PlayerAction.PlayPauseToggle) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = VinylBlack,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        IconButton(
            onClick = { onAction(PlayerAction.NextTrack) },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(VinylCardBg)
                .border(1.dp, VinylBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next Track",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PlaylistModalSheet(
    uiState: PlayerUiState,
    onClose: () -> Unit,
    onSelectTrack: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(VinylDarkGray)
                .border(1.dp, VinylBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .clickable(enabled = false) {}
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLAYLIST (${uiState.playlist.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Playlist",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(uiState.playlist) { idx, track ->
                    val isCurrent = idx == uiState.currentIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCurrent) VinylCardBg else Color.Transparent)
                            .border(
                                1.dp,
                                if (isCurrent) GoldAccent.copy(alpha = 0.5f) else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                onSelectTrack(idx)
                                onClose()
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isCurrent) GoldAccent else TextMuted
                            ),
                            modifier = Modifier.width(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isCurrent) GoldAccent else TextPrimary,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1
                            )
                        }
                        Text(
                            text = formatDuration(track.durationMs),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
