package com.app.neliofono.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.neliofono.model.PlayerAction
import com.app.neliofono.model.TrackInfo
import com.app.neliofono.ui.animation.VinylAnimationCoordinator
import com.app.neliofono.ui.animation.rememberVinylAnimationCoordinator
import com.app.neliofono.ui.components.DynamicVinylRecord
import com.app.neliofono.ui.components.KeyBadge
import com.app.neliofono.ui.components.TonearmView
import com.app.neliofono.ui.theme.GoldAccent
import com.app.neliofono.ui.theme.GoldAccentDark
import com.app.neliofono.ui.theme.KeyIndicatorActive
import com.app.neliofono.ui.theme.TextMuted
import com.app.neliofono.ui.theme.TextPrimary
import com.app.neliofono.ui.theme.TextSecondary
import com.app.neliofono.ui.theme.VinylBlack
import com.app.neliofono.ui.theme.VinylBorder
import com.app.neliofono.ui.theme.VinylCardBg
import com.app.neliofono.ui.theme.VinylDarkGray
import com.app.neliofono.viewmodel.PlayerUiState
import com.app.neliofono.viewmodel.PlayerViewModel
import com.app.neliofono.viewmodel.TransitionEvent
import java.util.Locale

@Composable
fun NeliofonoPlayerScreen(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val coordinator = rememberVinylAnimationCoordinator(
        isPlaying = uiState.isPlaying,
        onNextTrack = { viewModel.applyNextTrack() },
        onPrevTrack = { viewModel.applyPreviousTrack() }
    )

    LaunchedEffect(Unit) {
        viewModel.transitionEvents.collect { event ->
            when (event) {
                is TransitionEvent.Next -> coordinator.triggerTrackChange(forward = true)
                is TransitionEvent.Previous -> coordinator.triggerTrackChange(forward = false)
            }
        }
    }

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
            val aspectRatio = maxWidth / maxHeight
            val isLandscapeWide = aspectRatio > 1.15f

            if (isLandscapeWide) {
                LandscapeLayout(
                    uiState = uiState,
                    coordinator = coordinator,
                    onAction = { viewModel.handleAction(it) },
                    onSeek = { viewModel.seekTo(it) }
                )
            } else {
                SquarePortraitLayout(
                    uiState = uiState,
                    coordinator = coordinator,
                    onAction = { viewModel.handleAction(it) },
                    onSeek = { viewModel.seekTo(it) }
                )
            }

            // Playlist Overlay Modal (Y Button)
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
                            viewModel.applyNextTrack()
                        }
                    }
                )
            }

            // Help & Controls Guide Modal (START Button)
            AnimatedVisibility(
                visible = uiState.isHelpModalOpen,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                HelpGuideModalSheet(
                    uiState = uiState,
                    onClose = { viewModel.toggleHelpModal() }
                )
            }
        }
    }
}

@Composable
private fun SquarePortraitLayout(
    uiState: PlayerUiState,
    coordinator: VinylAnimationCoordinator,
    onAction: (PlayerAction) -> Unit,
    onSeek: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header
        AppHeader(
            onPlaylistClick = { onAction(PlayerAction.SwitchViewMode) },
            onHelpClick = { onAction(PlayerAction.ToggleHelpGuide) }
        )

        // Turntable Deck Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TurntableDeck(
                track = uiState.currentTrack,
                coordinator = coordinator,
                modifier = Modifier
                    .fillMaxHeight(0.96f)
                    .aspectRatio(1.15f)
            )
        }

        // Bottom Controls Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrackMetaSection(track = uiState.currentTrack)

            Spacer(modifier = Modifier.height(6.dp))

            SeekBarSection(
                currentPosMs = uiState.currentPositionMs,
                totalDurationMs = uiState.currentTrack.durationMs,
                onSeek = onSeek
            )

            Spacer(modifier = Modifier.height(6.dp))

            PlaybackControlButtons(
                isPlaying = uiState.isPlaying,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun LandscapeLayout(
    uiState: PlayerUiState,
    coordinator: VinylAnimationCoordinator,
    onAction: (PlayerAction) -> Unit,
    onSeek: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Turntable Deck
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            TurntableDeck(
                track = uiState.currentTrack,
                coordinator = coordinator,
                modifier = Modifier
                    .fillMaxHeight(0.96f)
                    .aspectRatio(1.15f)
            )
        }

        // Right Side: Header, Info, Controls
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader(
                onPlaylistClick = { onAction(PlayerAction.SwitchViewMode) },
                onHelpClick = { onAction(PlayerAction.ToggleHelpGuide) }
            )

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
        }
    }
}

@Composable
fun TurntableDeck(
    track: TrackInfo,
    coordinator: VinylAnimationCoordinator,
    modifier: Modifier = Modifier
) {
    var totalDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF181822),
                        Color(0xFF0F0F16)
                    )
                )
            )
            .border(1.dp, VinylBorder, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    },
                    onDragEnd = {
                        if (totalDrag < -60f) {
                            coordinator.triggerTrackChange(forward = true)
                        } else if (totalDrag > 60f) {
                            coordinator.triggerTrackChange(forward = false)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Vinyl Record placed on the Left side
        Box(
            modifier = Modifier
                .fillMaxHeight(0.90f)
                .aspectRatio(1f)
                .align(Alignment.CenterStart)
                .padding(start = 6.dp)
        ) {
            DynamicVinylRecord(
                track = track,
                rotation = coordinator.discRotation,
                palette = track.defaultPalette,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = coordinator.discOffsetX.value
                    }
            )
        }

        // Tonearm Overlay covering the deck
        TonearmView(
            angle = coordinator.armAngle.value,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AppHeader(
    onPlaylistClick: () -> Unit,
    onHelpClick: () -> Unit,
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onHelpClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "Controls Guide (START)",
                    tint = TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onPlaylistClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "Playlist (Y)",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
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
                .size(38.dp)
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

        Spacer(modifier = Modifier.width(24.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
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
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        IconButton(
            onClick = { onAction(PlayerAction.NextTrack) },
            modifier = Modifier
                .size(38.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HelpGuideModalSheet(
    uiState: PlayerUiState,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(VinylDarkGray)
                .border(1.dp, VinylBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .clickable(enabled = false) {}
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CONTROLS GUIDE",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeyBadge(keyLabel = "A", desc = "再生 / 一時停止")
                KeyBadge(keyLabel = "B", desc = "閉じる / 戻る")
                KeyBadge(keyLabel = "L1 / ◀", desc = "前の曲")
                KeyBadge(keyLabel = "R1 / ▶", desc = "次の曲")
                KeyBadge(keyLabel = "Y", desc = "プレイリスト")
                KeyBadge(keyLabel = "START", desc = "ガイド開閉")
            }

            if (uiState.recentKeyLogs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                val topLog = uiState.recentKeyLogs.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⚡ Last Input: [${topLog.timestampFormatted}] ${topLog.keyName} -> ${topLog.actionDescription}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = KeyIndicatorActive,
                            fontSize = 9.sp
                        ),
                        maxLines = 1
                    )
                }
            }
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
            .background(Color.Black.copy(alpha = 0.7f))
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
