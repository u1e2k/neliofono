package com.app.neliofono.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VinylAnimationCoordinator(
    private val scope: CoroutineScope,
    private val onNextTrack: () -> Unit,
    private val onPrevTrack: () -> Unit
) {
    // Disc Slide Offset (X座標)
    val discOffsetX = Animatable(0f)

    // Tonearm Angle (36f: レコード盤面上に着地, -6f: レコード盤から完全に離れて右外側アームレストへ退避)
    val armAngle = Animatable(36f)

    // Disc Spin Rotation (度数)
    var discRotation by mutableFloatStateOf(0f)
        private set

    var isTransitioning by mutableStateOf(false)
        private set

    fun updateArmForPlayback(isPlaying: Boolean) {
        if (isTransitioning) return
        scope.launch {
            val targetAngle = if (isPlaying) 36f else -6f
            armAngle.animateTo(
                targetValue = targetAngle,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }
    }

    fun triggerTrackChange(forward: Boolean) {
        if (isTransitioning) return
        isTransitioning = true

        scope.launch {
            // Phase 1: Retract Tonearm to right armrest (レコードから離す)
            armAngle.animateTo(
                targetValue = -6f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            )

            // Phase 2: Slide Out Current Record
            val slideOutTarget = if (forward) -900f else 900f
            discOffsetX.animateTo(
                targetValue = slideOutTarget,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )

            // Phase 3: Switch Track Data in ViewModel
            if (forward) onNextTrack() else onPrevTrack()

            // Phase 4: Prepare & Slide In New Record from opposite side
            val slideInStart = if (forward) 900f else -900f
            discOffsetX.snapTo(slideInStart)
            discOffsetX.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.78f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )

            // Phase 5: Drop Tonearm to Disc Surface (レコード盤の上へ降ろす)
            armAngle.animateTo(
                targetValue = 36f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )

            isTransitioning = false
        }
    }

    fun stepRotation(delta: Float) {
        discRotation = (discRotation + delta) % 360f
    }
}

@Composable
fun rememberVinylAnimationCoordinator(
    isPlaying: Boolean,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit
): VinylAnimationCoordinator {
    val scope = rememberCoroutineScope()
    val coordinator = remember {
        VinylAnimationCoordinator(
            scope = scope,
            onNextTrack = onNextTrack,
            onPrevTrack = onPrevTrack
        )
    }

    // Play/Pause arm reaction
    LaunchedEffect(isPlaying) {
        coordinator.updateArmForPlayback(isPlaying)
    }

    // Continuous 60fps GPU Spin loop when playing & arm is down
    LaunchedEffect(isPlaying, coordinator.isTransitioning) {
        if (isPlaying && !coordinator.isTransitioning) {
            var lastTime: Long = withFrameNanos { time: Long -> time }
            while (isActive) {
                withFrameNanos { frameTimeNanos: Long ->
                    val elapsedNanos = frameTimeNanos - lastTime
                    lastTime = frameTimeNanos
                    val dtSec = elapsedNanos / 1_000_000_000.0f
                    coordinator.stepRotation(180f * dtSec)
                }
            }
        }
    }

    return coordinator
}
