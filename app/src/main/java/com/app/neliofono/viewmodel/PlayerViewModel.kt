package com.app.neliofono.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.neliofono.model.KeyLogEntry
import com.app.neliofono.model.PlayerAction
import com.app.neliofono.model.TrackInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val recentKeyLogs: List<KeyLogEntry> = emptyList(),
    val latestKeyAction: String? = null
)

class PlayerViewModel : ViewModel() {

    private val sampleTracks = listOf(
        TrackInfo(
            id = "1",
            title = "Midnight Horizon (Vinyl Edit)",
            artist = "Aether Resonance",
            album = "Neliö Soundscapes Vol. 1",
            durationMs = 214000L
        ),
        TrackInfo(
            id = "2",
            title = "Rotating Shadows (45 RPM)",
            artist = "Nordic Groove Collective",
            album = "Square Wave Symphony",
            durationMs = 185000L
        ),
        TrackInfo(
            id = "3",
            title = "Analog Odyssey",
            artist = "Kurogane Soundworks",
            album = "RG Retro Sessions",
            durationMs = 248000L
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

    private var playbackTimerJob: Job? = null

    init {
        startPlaybackSimulation()
    }

    fun handleAction(action: PlayerAction) {
        when (action) {
            is PlayerAction.PlayPauseToggle -> togglePlayPause()
            is PlayerAction.NextTrack -> nextTrack()
            is PlayerAction.PreviousTrack -> previousTrack()
            is PlayerAction.SwitchViewMode -> toggleViewMode()
            is PlayerAction.RawKeyInput -> {
                // Keep UI updated with key info
            }
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

    fun nextTrack() {
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

    fun previousTrack() {
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
