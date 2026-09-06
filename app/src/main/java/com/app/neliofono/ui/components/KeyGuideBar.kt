package com.app.neliofono.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.neliofono.model.KeyLogEntry
import com.app.neliofono.ui.theme.GoldAccent
import com.app.neliofono.ui.theme.KeyIndicatorActive
import com.app.neliofono.ui.theme.KeyIndicatorBg
import com.app.neliofono.ui.theme.TextMuted
import com.app.neliofono.ui.theme.TextPrimary
import com.app.neliofono.ui.theme.TextSecondary
import com.app.neliofono.ui.theme.VinylBorder
import com.app.neliofono.ui.theme.VinylCardBg

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeyGuideBar(
    recentLogs: List<KeyLogEntry>,
    latestKeyAction: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VinylCardBg.copy(alpha = 0.85f))
            .border(1.dp, VinylBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        // Physical Key Legend Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = "Physical Controller",
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RG ROTATE CONTROLS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldAccent,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (latestKeyAction != null) {
                Text(
                    text = "Active: $latestKeyAction",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = KeyIndicatorActive,
                        fontSize = 9.sp
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Key Buttons Guide Badges
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeyBadge(keyLabel = "A", desc = "再生/停止")
            KeyBadge(keyLabel = "L1 / ◀", desc = "前曲")
            KeyBadge(keyLabel = "R1 / ▶", desc = "次曲")
            KeyBadge(keyLabel = "Y", desc = "リスト切替")
        }

        // Live Key Log Feedback
        if (recentLogs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val topLog = recentLogs.first()
                Text(
                    text = "⚡ [${topLog.timestampFormatted}] ${topLog.keyName} -> ${topLog.actionDescription}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        fontSize = 9.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun KeyBadge(
    keyLabel: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(KeyIndicatorBg)
            .border(1.dp, VinylBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(GoldAccent.copy(alpha = 0.2f))
                .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = keyLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                )
            )
        }
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextPrimary,
                fontSize = 9.sp
            )
        )
    }
}
