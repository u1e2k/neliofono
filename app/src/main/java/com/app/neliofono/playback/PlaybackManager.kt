package com.app.neliofono.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.app.neliofono.model.RepeatMode
import com.app.neliofono.model.TrackInfo
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _currentMediaIndex = MutableStateFlow(0)
    val currentMediaIndex: StateFlow<Int> = _currentMediaIndex.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var positionTrackerJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) {
                startPositionTracker()
            } else {
                stopPositionTracker()
                mediaController?.let { _currentPositionMs.value = it.currentPosition.coerceAtLeast(0L) }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaController?.let { controller ->
                _currentMediaIndex.value = controller.currentMediaItemIndex
                _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                _isPlaying.value = false
                stopPositionTracker()
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                else -> RepeatMode.OFF
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _isShuffleEnabled.value = shuffleModeEnabled
        }
    }

    fun connect() {
        if (mediaController != null || controllerFuture != null) return

        val sessionToken = SessionToken(
            context,
            ComponentName(context, NeliofonoPlaybackService::class.java)
        )
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future

        future.addListener(
            {
                try {
                    val controller = future.get()
                    mediaController = controller
                    controller.addListener(playerListener)
                    _isConnected.value = true
                    _isPlaying.value = controller.isPlaying
                    _currentMediaIndex.value = controller.currentMediaItemIndex
                    _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L)
                    _repeatMode.value = when (controller.repeatMode) {
                        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                        else -> RepeatMode.OFF
                    }
                    _isShuffleEnabled.value = controller.shuffleModeEnabled
                    if (controller.isPlaying) {
                        startPositionTracker()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    fun setPlaylist(tracks: List<TrackInfo>, startIndex: Int = 0, playWhenReady: Boolean = false) {
        val controller = mediaController ?: return
        if (tracks.isEmpty()) return

        val mediaItems = tracks.map { it.toMediaItem() }
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.playWhenReady = playWhenReady
        _currentMediaIndex.value = startIndex
        _currentPositionMs.value = 0L
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        val controller = mediaController ?: return
        val exoMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        controller.repeatMode = exoMode
        _repeatMode.value = mode
    }

    fun cycleRepeatMode(): RepeatMode {
        val nextMode = _repeatMode.value.next()
        setRepeatMode(nextMode)
        return nextMode
    }

    fun setShuffleModeEnabled(enabled: Boolean) {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = enabled
        _isShuffleEnabled.value = enabled
    }

    fun toggleShuffleMode(): Boolean {
        val nextShuffle = !_isShuffleEnabled.value
        setShuffleModeEnabled(nextShuffle)
        return nextShuffle
    }

    fun seekToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else if (controller.mediaItemCount > 0) {
            controller.seekToDefaultPosition(0)
        }
    }

    fun seekToPrevious() {
        val controller = mediaController ?: return
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else if (controller.mediaItemCount > 0) {
            controller.seekToDefaultPosition(controller.mediaItemCount - 1)
        }
    }

    fun seekToTrackIndex(index: Int) {
        val controller = mediaController ?: return
        if (index in 0 until controller.mediaItemCount) {
            controller.seekToDefaultPosition(index)
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        controller.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    private fun startPositionTracker() {
        positionTrackerJob?.cancel()
        positionTrackerJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L)
                    }
                }
                delay(200)
            }
        }
    }

    private fun stopPositionTracker() {
        positionTrackerJob?.cancel()
        positionTrackerJob = null
    }

    fun release() {
        stopPositionTracker()
        mediaController?.let { controller ->
            controller.removeListener(playerListener)
            controller.release()
        }
        mediaController = null
        controllerFuture = null
        _isConnected.value = false
    }
}
