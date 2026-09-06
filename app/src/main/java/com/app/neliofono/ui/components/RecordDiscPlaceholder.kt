package com.app.neliofono.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.neliofono.ui.theme.GoldAccent
import com.app.neliofono.ui.theme.GoldAccentDark
import com.app.neliofono.ui.theme.RecordCenter
import com.app.neliofono.ui.theme.VinylBlack
import com.app.neliofono.ui.theme.VinylBorder
import com.app.neliofono.ui.theme.VinylDarkGray

@Composable
fun RecordDiscPlaceholder(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VinylSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAngle"
    )

    val currentRotation = if (isPlaying) rotation else 0f

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(6.dp)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(VinylBlack)
            .border(2.dp, VinylBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Vinyl Grooves (溝)
        Canvas(modifier = Modifier.fillMaxSize().rotate(currentRotation)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            // Vinyl Base Background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF22222E),
                        Color(0xFF101017),
                        Color(0xFF08080C)
                    ),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            // Vinyl Grooves Rings
            val grooveCount = 16
            val minRadius = maxRadius * 0.40f
            val step = (maxRadius - minRadius) / grooveCount

            for (i in 0 until grooveCount) {
                val r = minRadius + i * step
                drawCircle(
                    color = Color.White.copy(alpha = if (i % 3 == 0) 0.08f else 0.03f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.0f)
                )
            }

            // Light Reflection Sheen (レコードの光沢反射)
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.0f),
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.0f),
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.0f)
                    ),
                    center = center
                ),
                radius = maxRadius,
                center = center
            )
        }

        // Center Label (45 RPM Label)
        Box(
            modifier = Modifier
                .fillMaxSize(0.40f)
                .rotate(currentRotation)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            RecordCenter,
                            Color(0xFF7A130F)
                        )
                    )
                )
                .border(1.5.dp, GoldAccent.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NELIOFONO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldAccent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                Text(
                    text = "45 RPM",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 14.dp)
                )
            }

            // Center Spindle Hole
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(VinylDarkGray)
                    .border(1.dp, GoldAccentDark, CircleShape)
            )
        }
    }
}
