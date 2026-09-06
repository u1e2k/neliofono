package com.app.neliofono.input

import android.util.Log
import android.view.KeyEvent
import com.app.neliofono.model.KeyLogEntry
import com.app.neliofono.model.PlayerAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KeyEventHandler(
    private val onActionTriggered: (PlayerAction) -> Unit,
    private val onKeyLog: (KeyLogEntry) -> Unit
) {

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        val keyCode = event.keyCode
        val (action, actionDesc) = mapKeyCodeToAction(keyCode)
        val keyName = getKeyName(keyCode)

        Log.d(TAG, "Key Event Intercepted: $keyName (code=$keyCode) -> $actionDesc")

        val logEntry = KeyLogEntry(
            keyName = keyName,
            keyCode = keyCode,
            actionDescription = actionDesc,
            timestampFormatted = timeFormat.format(Date())
        )

        onKeyLog(logEntry)
        onActionTriggered(action)

        return true
    }

    private fun mapKeyCodeToAction(keyCode: Int): Pair<PlayerAction, String> {
        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                PlayerAction.NextTrack to "Next Track (次へ)"
            }
            KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_DPAD_LEFT -> {
                PlayerAction.PreviousTrack to "Previous Track (前へ)"
            }
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_SPACE -> {
                PlayerAction.PlayPauseToggle to "Play / Pause (再生/一時停止)"
            }
            KeyEvent.KEYCODE_BUTTON_Y -> {
                PlayerAction.SwitchViewMode to "Switch View (表示切替)"
            }
            KeyEvent.KEYCODE_BUTTON_B -> {
                PlayerAction.RawKeyInput(keyCode, "B") to "Button B"
            }
            KeyEvent.KEYCODE_BUTTON_X -> {
                PlayerAction.RawKeyInput(keyCode, "X") to "Button X"
            }
            KeyEvent.KEYCODE_BUTTON_START -> {
                PlayerAction.RawKeyInput(keyCode, "START") to "START Button"
            }
            KeyEvent.KEYCODE_BUTTON_SELECT -> {
                PlayerAction.RawKeyInput(keyCode, "SELECT") to "SELECT Button"
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                PlayerAction.RawKeyInput(keyCode, "DPAD_UP") to "DPad Up"
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                PlayerAction.RawKeyInput(keyCode, "DPAD_DOWN") to "DPad Down"
            }
            KeyEvent.KEYCODE_BUTTON_L2 -> {
                PlayerAction.RawKeyInput(keyCode, "L2") to "Trigger L2"
            }
            KeyEvent.KEYCODE_BUTTON_R2 -> {
                PlayerAction.RawKeyInput(keyCode, "R2") to "Trigger R2"
            }
            else -> {
                val name = KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_")
                PlayerAction.RawKeyInput(keyCode, name) to "Key: $name"
            }
        }
    }

    private fun getKeyName(keyCode: Int): String {
        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> "BUTTON_A"
            KeyEvent.KEYCODE_BUTTON_B -> "BUTTON_B"
            KeyEvent.KEYCODE_BUTTON_X -> "BUTTON_X"
            KeyEvent.KEYCODE_BUTTON_Y -> "BUTTON_Y"
            KeyEvent.KEYCODE_BUTTON_L1 -> "BUTTON_L1"
            KeyEvent.KEYCODE_BUTTON_R1 -> "BUTTON_R1"
            KeyEvent.KEYCODE_BUTTON_L2 -> "BUTTON_L2"
            KeyEvent.KEYCODE_BUTTON_R2 -> "BUTTON_R2"
            KeyEvent.KEYCODE_BUTTON_START -> "START"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "SELECT"
            KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> "MEDIA_PLAY_PAUSE"
            else -> KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_")
        }
    }

    companion object {
        private const val TAG = "NeliofonoKey"
    }
}
