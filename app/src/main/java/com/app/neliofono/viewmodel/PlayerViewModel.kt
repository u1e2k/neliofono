package com.app.neliofono.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.neliofono.data.AudioScanner
import com.app.neliofono.model.KeyLogEntry
import com.app.neliofono.model.PlayerAction
import com.app.neliofono.model.TrackInfo
import com.app.neliofono.model.VinylPalette
import com.app.neliofono.playback.PlaybackManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentTrack: TrackInfo,
    val playlist: List<TrackInfo>,
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val isPlaylistViewOpen: Boolean = false,
    val isHelpModalOpen: Boolean = false,
    val isLocalAudioLoaded: Boolean = false,
    val recentKeyLogs: List<KeyLogEntry> = emptyList(),
    val latestKeyAction: String? = null
)

sealed interface TransitionEvent {
    data class Next(val animate: Boolean = true) : TransitionEvent
    data class Previous(val animate: Boolean = true) : TransitionEvent
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val sampleTracks = listOf(
        TrackInfo(
            id = "sample_1",
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
            id = "sample_2",
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
            id = "sample_3",
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
            id = "sample_4",
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

    private val audioScanner = AudioScanner(application)
    private val playbackManager = PlaybackManager(application, viewModelScope)

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            currentTrack = sampleTracks[0],
            playlist = sampleTracks,
            currentIndex = 0,
            isPlaying = false,
            currentPositionMs = 0L
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _transitionEvents = MutableSharedFlow<TransitionEvent>(extraBufferCapacity = 1)
    val transitionEvents: SharedFlow<TransitionEvent> = _transitionEvents.asSharedFlow()

    init {
        playbackManager.connect()
        observePlaybackManager()
        loadLocalAudio()
    }

    private fun observePlaybackManager() {
        viewModelScope.launch {
            playbackManager.isPlaying.collect { playing ->
                _uiState.update { it.copy(isPlaying = playing) }
            }
        }

        viewModelScope.launch {
            playbackManager.currentPositionMs.collect { pos ->
                _uiState.update { it.copy(currentPositionMs = pos) }
            }
        }

        viewModelScope.launch {
            playbackManager.currentMediaIndex.collect { index ->
                _uiState.update { state ->
                    if (index in state.playlist.indices) {
                        state.copy(
                            currentIndex = index,
                            currentTrack = state.playlist[index]
                        )
                    } else {
                        state
                    }
                }
            }
        }

        viewModelScope.launch {
            playbackManager.isConnected.collect { connected ->
                if (connected) {
                    val currentTracks = _uiState.value.playlist
                    playbackManager.setPlaylist(
                        tracks = currentTracks,
                        startIndex = _uiState.value.currentIndex,
                        playWhenReady = false
                    )
                }
            }
        }
    }

    fun loadLocalAudio() {
        viewModelScope.launch {
            val scanned = audioScanner.scanAudioFiles()
            if (scanned.isNotEmpty()) {
                _uiState.update { state ->
                    state.copy(
                        playlist = scanned,
                        currentTrack = scanned[0],
                        currentIndex = 0,
                        isLocalAudioLoaded = true,
                        currentPositionMs = 0L
                    )
                }
                if (playbackManager.isConnected.value) {
                    playbackManager.setPlaylist(scanned, startIndex = 0, playWhenReady = false)
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        playlist = sampleTracks,
                        currentTrack = sampleTracks[0],
                        currentIndex = 0,
                        isLocalAudioLoaded = false
                    )
                }
            }
        }
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
        if (playbackManager.isConnected.value && _uiState.value.isLocalAudioLoaded) {
            playbackManager.seekToNext()
        } else {
            _uiState.update { state ->
                val nextIdx = (state.currentIndex + 1) % state.playlist.size
                state.copy(
                    currentIndex = nextIdx,
                    currentTrack = state.playlist[nextIdx],
                    currentPositionMs = 0L
                )
            }
        }
    }

    fun applyPreviousTrack() {
        if (playbackManager.isConnected.value && _uiState.value.isLocalAudioLoaded) {
            playbackManager.seekToPrevious()
        } else {
            _uiState.update { state ->
                val prevIdx = if (state.currentIndex - 1 < 0) state.playlist.size - 1 else state.currentIndex - 1
                state.copy(
                    currentIndex = prevIdx,
                    currentTrack = state.playlist[prevIdx],
                    currentPositionMs = 0L
                )
            }
        }
    }

    fun selectTrack(index: Int) {
        if (index in _uiState.value.playlist.indices) {
            if (playbackManager.isConnected.value && _uiState.value.isLocalAudioLoaded) {
                playbackManager.seekToTrackIndex(index)
            } else {
                _uiState.update { state ->
                    state.copy(
                        currentIndex = index,
                        currentTrack = state.playlist[index],
                        currentPositionMs = 0L
                    )
                }
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
        if (playbackManager.isConnected.value && _uiState.value.isLocalAudioLoaded) {
            playbackManager.togglePlayPause()
        } else {
            _uiState.update { it.copy(isPlaying = !it.isPlaying) }
        }
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
        if (playbackManager.isConnected.value && _uiState.value.isLocalAudioLoaded) {
            playbackManager.seekTo(positionMs)
        } else {
            _uiState.update { it.copy(currentPositionMs = positionMs.coerceIn(0L, it.currentTrack.durationMs)) }
        }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isPlaylistViewOpen = !it.isPlaylistViewOpen) }
    }

    override fun onCleared() {
        super.onCleared()
        playbackManager.release()
    }
}
