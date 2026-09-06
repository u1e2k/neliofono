package com.app.neliofono.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.app.neliofono.ui.theme.GoldAccent
import com.app.neliofono.ui.theme.GoldAccentDark
import com.app.neliofono.ui.theme.GoldAccentLight

/**
 * 完全ストレート型トーンアーム（パイプと針が一直線）。
 *
 * @param angle
 *   - 停止時 (6f): 右側の固定アームレストへ退避（レコード盤から完全に離れる）
 *   - 再生時 (-36f): 左へ旋回し、針先がレコード盤面の音溝の上に乗る
 */
@Composable
fun TonearmView(
    angle: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // ピボット（支点）座標：右上の盤外コーナー
            val pivotX = w * 0.85f
            val pivotY = h * 0.16f
            val pivotRadius = w * 0.052f
            val armLength = h * 0.45f

            // 1. 固定アームレスト台（停止時に針が乗る右外側のポスト）
            val restAngleRad = Math.toRadians(6.0)
            val restPostX = pivotX + (armLength * Math.sin(restAngleRad)).toFloat()
            val restPostY = pivotY + (armLength * Math.cos(restAngleRad)).toFloat()

            drawRoundRect(
                color = Color(0xFF22222E),
                topLeft = Offset(restPostX - 6f, restPostY - 6f),
                size = androidx.compose.ui.geometry.Size(12f, 20f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            drawCircle(
                color = GoldAccent.copy(alpha = 0.6f),
                radius = 3.5f,
                center = Offset(restPostX, restPostY)
            )

            // 2. 固定ピボット台座（ジンバルベース）
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF424254),
                        Color(0xFF20202A),
                        Color(0xFF0F0F16)
                    ),
                    center = Offset(pivotX, pivotY),
                    radius = pivotRadius * 1.8f
                ),
                radius = pivotRadius * 1.8f,
                center = Offset(pivotX, pivotY)
            )
            drawCircle(
                color = GoldAccentDark.copy(alpha = 0.8f),
                radius = pivotRadius * 1.8f,
                center = Offset(pivotX, pivotY),
                style = Stroke(width = 1.6f)
            )

            // 3. 回転アーム部（ストレートパイプ & 一体型ヘッドシェル）
            withTransform({
                rotate(
                    degrees = angle,
                    pivot = Offset(pivotX, pivotY)
                )
            }) {
                // カウンターウェイト（ピボットの真上方向）
                val cwY = pivotY - h * 0.07f
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1E1E28),
                            Color(0xFF6B6B82),
                            Color(0xFF9E9EB2),
                            Color(0xFF333342),
                            Color(0xFF181822)
                        ),
                        startX = pivotX - 16f,
                        endX = pivotX + 16f
                    ),
                    topLeft = Offset(pivotX - 16f, cwY - 8f),
                    size = androidx.compose.ui.geometry.Size(32f, 16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                drawLine(
                    color = GoldAccent,
                    start = Offset(pivotX - 16f, cwY),
                    end = Offset(pivotX + 16f, cwY),
                    strokeWidth = 2f
                )

                // まっすぐ伸びるストレートパイプの終端（ピボットから真下方向）
                val endPipeY = pivotY + armLength

                // アームの影
                drawLine(
                    color = Color.Black.copy(alpha = 0.45f),
                    start = Offset(pivotX + 2f, pivotY + 2f),
                    end = Offset(pivotX + 2f, endPipeY + 2f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )

                // アーム本体（ゴールドメタル調・完全直線ストレートパイプ）
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GoldAccentLight,
                            GoldAccent,
                            GoldAccentDark,
                            Color(0xFF8C6424)
                        ),
                        start = Offset(pivotX, pivotY),
                        end = Offset(pivotX, endPipeY)
                    ),
                    start = Offset(pivotX, pivotY),
                    end = Offset(pivotX, endPipeY),
                    strokeWidth = 4.2f,
                    cap = StrokeCap.Round
                )

                // 一体型ヘッドシェル（ストレートの延長線上にまっすぐ配置）
                val headY = endPipeY
                val headshellPath = Path().apply {
                    moveTo(pivotX - 5f, headY)
                    lineTo(pivotX - 6f, headY + h * 0.075f)
                    lineTo(pivotX + 6f, headY + h * 0.075f)
                    lineTo(pivotX + 5f, headY)
                    close()
                }

                // ヘッドシェル本体
                drawPath(
                    path = headshellPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A2A38),
                            Color(0xFF14141E)
                        ),
                        startY = headY,
                        endY = headY + h * 0.075f
                    ),
                    style = Fill
                )
                drawPath(
                    path = headshellPath,
                    color = GoldAccent.copy(alpha = 0.7f),
                    style = Stroke(width = 1.2f)
                )

                // 指掛けフック（Finger Lift）
                val fingerPath = Path().apply {
                    moveTo(pivotX + 5f, headY + h * 0.035f)
                    quadraticBezierTo(pivotX + 15f, headY + h * 0.03f, pivotX + 13f, headY + h * 0.01f)
                }
                drawPath(
                    path = fingerPath,
                    color = GoldAccentLight,
                    style = Stroke(width = 1.8f, cap = StrokeCap.Round)
                )

                // 赤いスタイラス針先インジケーター（ヘッドシェル先端の中心）
                drawCircle(
                    color = Color(0xFFFF3B30),
                    radius = 2.2f,
                    center = Offset(pivotX, headY + h * 0.078f)
                )

                // ピボット真鍮ドーム
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldAccentLight,
                            GoldAccent,
                            GoldAccentDark
                        ),
                        center = Offset(pivotX, pivotY),
                        radius = pivotRadius * 0.7f
                    ),
                    radius = pivotRadius * 0.7f,
                    center = Offset(pivotX, pivotY)
                )
            }
        }
    }
}
