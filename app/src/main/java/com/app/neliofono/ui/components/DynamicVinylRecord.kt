package com.app.neliofono.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.neliofono.model.TrackInfo
import com.app.neliofono.model.VinylPalette
import com.app.neliofono.ui.theme.GoldAccent
import com.app.neliofono.ui.theme.GoldAccentDark
import com.app.neliofono.ui.theme.VinylBlack
import com.app.neliofono.ui.theme.VinylBorder
import com.app.neliofono.ui.theme.VinylDarkGray

@Composable
fun DynamicVinylRecord(
    track: TrackInfo,
    rotation: Float,
    modifier: Modifier = Modifier,
    palette: VinylPalette = track.defaultPalette
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(6.dp)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .background(VinylBlack)
            .border(2.dp, VinylBorder, CircleShape)
            .graphicsLayer {
                rotationZ = rotation
            },
        contentAlignment = Alignment.Center
    ) {
        // Vinyl Grooves & Dynamic Swirl Patterns
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            // 1. Deep Vinyl Base
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF20202A),
                        Color(0xFF101016),
                        Color(0xFF08080C)
                    ),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            // 2. Dynamic Streamlined Swirl (抽出色を活かした流線形・渦巻きスイープグラデーション)
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        palette.darkVibrant.copy(alpha = 0.45f),
                        palette.dominant.copy(alpha = 0.30f),
                        Color.Transparent,
                        palette.vibrant.copy(alpha = 0.35f),
                        palette.darkVibrant.copy(alpha = 0.45f)
                    ),
                    center = center
                ),
                radius = maxRadius,
                center = center
            )

            // 3. Realistic Vinyl Grooves (同心円の溝)
            val grooveCount = 20
            val minRadius = maxRadius * 0.38f
            val step = (maxRadius - minRadius) / grooveCount

            for (i in 0 until grooveCount) {
                val r = minRadius + i * step
                val alpha = if (i % 4 == 0) 0.09f else 0.035f
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.0f)
                )
            }

            // 4. Double Light Sheen Reflection (レコードの光沢反射)
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.0f),
                        Color.White.copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.0f),
                        Color.White.copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.0f)
                    ),
                    center = center
                ),
                radius = maxRadius,
                center = center
            )
        }

        // Center Label (レーベル)
        Box(
            modifier = Modifier
                .fillMaxSize(0.38f)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.dominant,
                            palette.darkVibrant
                        )
                    )
                )
                .border(1.5.dp, palette.lightMuted.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // If coverUrl exists, show circular album art in center
            if (track.coverUrl != null) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = track.album,
                    modifier = Modifier
                        .fillMaxSize(0.85f)
                        .clip(CircleShape)
                )
            } else {
                // Procedural Elegant Label Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NELIOFONO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.lightMuted,
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
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }
            }

            // Center Spindle Hole & Brass Ring
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(VinylDarkGray)
                    .border(1.2.dp, GoldAccentDark, CircleShape)
            )
        }
    }
}
