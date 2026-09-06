package com.app.neliofono.model

import androidx.compose.ui.graphics.Color

data class VinylPalette(
    val dominant: Color,
    val vibrant: Color,
    val darkVibrant: Color,
    val lightMuted: Color
)

data class TrackInfo(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val coverUrl: String? = null,
    val defaultPalette: VinylPalette = VinylPalette(
        dominant = Color(0xFF9E2A2B),
        vibrant = Color(0xFFE05A47),
        darkVibrant = Color(0xFF540B0E),
        lightMuted = Color(0xFFE5B061)
    )
)

sealed interface PlayerAction {
    data object PlayPauseToggle : PlayerAction
    data object NextTrack : PlayerAction
    data object PreviousTrack : PlayerAction
    data object SwitchViewMode : PlayerAction
    data object ToggleHelpGuide : PlayerAction
    data object DismissOverlayOrBack : PlayerAction
    data class RawKeyInput(val keyCode: Int, val keyName: String) : PlayerAction
}

data class KeyLogEntry(
    val id: Long = System.currentTimeMillis(),
    val keyName: String,
    val keyCode: Int,
    val actionDescription: String,
    val timestampFormatted: String
)
