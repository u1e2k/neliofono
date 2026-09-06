package com.app.neliofono

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.app.neliofono.input.KeyEventHandler
import com.app.neliofono.ui.NeliofonoPlayerScreen
import com.app.neliofono.ui.theme.NeliofonoTheme
import com.app.neliofono.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()

    private val keyEventHandler by lazy {
        KeyEventHandler(
            onActionTriggered = { action ->
                playerViewModel.handleAction(action)
            },
            onKeyLog = { logEntry ->
                playerViewModel.addKeyLog(logEntry)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeliofonoTheme {
                NeliofonoPlayerScreen(viewModel = playerViewModel)
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val handled = keyEventHandler.handleKeyEvent(event)
        if (handled) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}
