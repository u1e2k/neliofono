package com.app.neliofono.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.neliofono.model.KeyLogEntry
import com.app.neliofono.model.PlayerAction
import com.app.neliofono.model.TrackInfo
import com.app.neliofono.model.VinylPalette
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentTrack: TrackInfo,
    val playlist: List<TrackInfo>,
    val currentIndex: Int = 0,
    val isPlaying: Boolean = true,
    val currentPositionMs: Long = 0L,
    val isPlaylistViewOpen: Boolean = false,
    val isHelpModalOpen: Boolean = false,
    val recentKeyLogs: List<KeyLogEntry> = emptyList(),
    val latestKeyAction: String? = null
)

sealed interface TransitionEvent {
    data class Next(val animate: Boolean = true) : TransitionEvent
    data class Previous(val animate: Boolean = true) : TransitionEvent
}

class PlayerViewModel : ViewModel() {

    private val sampleTracks = listOf(
        TrackInfo(
            id = "1",
            title = "Midnight Horizon (Vinyl Edit)",
            artist = "Aether Resonance",
            album = "Neliö Soundscapes Vol. 1",
            durationMs = 214000L,
            defaultPalette = VinylPalette(
                dominant = Color(0xFF8B1E3F),
                vibrant = Color(0xFFD94F70),
                darkVibrant = Color(0xFF3F0A1D),
                lightMuted = Color(0xFFE89BA7)
            )
        ),
        TrackInfo(
            id = "2",
            title = "Rotating Shadows (45 RPM)",
            artist = "Nordic Groove Collective",
            album = "Square Wave Symphony",
            durationMs = 185000L,
            defaultPalette = VinylPalette(
                dominant = Color(0xFF1E4D6B),
                vibrant = Color(0xFF3897C5),
                darkVibrant = Color(0xFF0D2535),
                lightMuted = Color(0xFF8EC5E0)
            )
        ),
        TrackInfo(
            id = "3",
            title = "Analog Odyssey (Amber Glow)",
            artist = "Kurogane Soundworks",
            album = "RG Retro Sessions",
            durationMs = 248000L,
            defaultPalette = VinylPalette(
                dominant = Color(0xFFB86B1B),
                vibrant = Color(0xFFE89A3C),
                darkVibrant = Color(0xFF5E3206),
                lightMuted = Color(0xFFFFD580)
            )
        ),
        TrackInfo(
            id = "4",
            title = "Emerald Frequency",
            artist = "Neo Tokyo Jazz Unit",
            album = "Modular Garden",
            durationMs = 196000L,
            defaultPalette = VinylPalette(
                dominant = Color(0xFF1A5E42),
                vibrant = Color(0xFF2EB886),
                darkVibrant = Color(0xFF0B2E20),
                lightMuted = Color(0xFFA3E5CB)
            )
        )
    )

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            currentTrack = sampleTracks[0],
            playlist = sampleTracks,
            currentIndex = 0,
            isPlaying = true,
            currentPositionMs = 35000L
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _transitionEvents = MutableSharedFlow<TransitionEvent>(extraBufferCapacity = 1)
    val transitionEvents: SharedFlow<TransitionEvent> = _transitionEvents.asSharedFlow()

    private var playbackTimerJob: Job? = null

    init {
        startPlaybackSimulation()
    }

    fun handleAction(action: PlayerAction) {
        when (action) {
            is PlayerAction.PlayPauseToggle -> togglePlayPause()
            is PlayerAction.NextTrack -> requestNextTrack()
            is PlayerAction.PreviousTrack -> requestPreviousTrack()
            is PlayerAction.SwitchViewMode -> toggleViewMode()
            is PlayerAction.ToggleHelpGuide -> toggleHelpModal()
            is PlayerAction.DismissOverlayOrBack -> dismissOverlay()
            is PlayerAction.RawKeyInput -> {
                // Key logged in HUD
            }
        }
    }

    fun requestNextTrack() {
        _transitionEvents.tryEmit(TransitionEvent.Next(animate = true))
    }

    fun requestPreviousTrack() {
        _transitionEvents.tryEmit(TransitionEvent.Previous(animate = true))
    }

    fun applyNextTrack() {
        _uiState.update { state ->
            val nextIdx = (state.currentIndex + 1) % state.playlist.size
            state.copy(
                currentIndex = nextIdx,
                currentTrack = state.playlist[nextIdx],
                currentPositionMs = 0L,
                isPlaying = true
            )
        }
    }

    fun applyPreviousTrack() {
        _uiState.update { state ->
            val prevIdx = if (state.currentIndex - 1 < 0) state.playlist.size - 1 else state.currentIndex - 1
            state.copy(
                currentIndex = prevIdx,
                currentTrack = state.playlist[prevIdx],
                currentPositionMs = 0L,
                isPlaying = true
            )
        }
    }

    fun addKeyLog(entry: KeyLogEntry) {
        _uiState.update { state ->
            val updatedLogs = (listOf(entry) + state.recentKeyLogs).take(6)
            state.copy(
                recentKeyLogs = updatedLogs,
                latestKeyAction = "${entry.keyName} -> ${entry.actionDescription}"
            )
        }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun toggleHelpModal() {
        _uiState.update { it.copy(isHelpModalOpen = !it.isHelpModalOpen) }
    }

    fun dismissOverlay() {
        _uiState.update { state ->
            if (state.isHelpModalOpen || state.isPlaylistViewOpen) {
                state.copy(
                    isHelpModalOpen = false,
                    isPlaylistViewOpen = false
                )
            } else {
                state
            }
        }
    }

    fun seekTo(positionMs: Long) {
        _uiState.update { it.copy(currentPositionMs = positionMs.coerceIn(0L, it.currentTrack.durationMs)) }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isPlaylistViewOpen = !it.isPlaylistViewOpen) }
    }

    private fun startPlaybackSimulation() {
        playbackTimerJob?.cancel()
        playbackTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_uiState.value.isPlaying) {
                    _uiState.update { state ->
                        val nextPos = state.currentPositionMs + 1000L
                        if (nextPos >= state.currentTrack.durationMs) {
                            val nextIdx = (state.currentIndex + 1) % state.playlist.size
                            state.copy(
                                currentIndex = nextIdx,
                                currentTrack = state.playlist[nextIdx],
                                currentPositionMs = 0L
                            )
                        } else {
                            state.copy(currentPositionMs = nextPos)
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackTimerJob?.cancel()
    }
}
