package com.app.neliofono.model

data class TrackInfo(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val coverUrl: String? = null
)

sealed interface PlayerAction {
    data object PlayPauseToggle : PlayerAction
    data object NextTrack : PlayerAction
    data object PreviousTrack : PlayerAction
    data object SwitchViewMode : PlayerAction
    data class RawKeyInput(val keyCode: Int, val keyName: String) : PlayerAction
}

data class KeyLogEntry(
    val id: Long = System.currentTimeMillis(),
    val keyName: String,
    val keyCode: Int,
    val actionDescription: String,
    val timestampFormatted: String
)
