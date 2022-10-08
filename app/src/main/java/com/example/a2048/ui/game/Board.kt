package com.example.a2048.ui.game

import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.luminance
import kotlin.math.pow

fun interpolate(
    x: Float,
    inRange: IntRange,
    outRange: ClosedFloatingPointRange<Float>,
): Float = outRange.start * (outRange.endInclusive / outRange.start).pow(
    (x - inRange.first) / (inRange.last - inRange.first))

@Composable
fun Board(board: BoardState, boardSize: Int) {
    val padding = 24f
    val totalPadding = (boardSize - 1) * padding

    val surfaceColor = MaterialTheme.colorScheme.surface
    // val cellColors = mapOf(
    //     0 to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
    //     2 to Color(0xFFEEE4DA),
    //     4 to Color(0xFFEEE1C9),
    //     8 to Color(0xFFF3B27A),
    //     16 to Color(0xFFF69664),
    //     32 to Color(0xFFF77C5F),
    //     64 to Color(0xFFF75F3B),
    //     128 to Color(0xFFEDD073),
    //     256 to Color(0xFFEDCC62),
    //     512 to Color(0xFFEDC950),
    //     1024 to Color(0xFFEDC53F),
    //     2048 to Color(0xFFEDC22E),
    // )
    val cellColors: Map<Int, Color> =
        mutableListOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)).apply {
            addAll(
                (1..6).map {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = interpolate(it.toFloat(),
                        1..6,
                        0.1f..0.9f))
                }
            )
            addAll(
                (7..11).map {
                    MaterialTheme.colorScheme.primary.copy(alpha = interpolate(it.toFloat(),
                        7..11,
                        0.6f..0.9f))
                }
            )
        }.withIndex().associate {
            val index = 2f.pow(it.index).toInt()
            Pair(if (index == 1) 0 else index, it.value)
        }
    val cornerRadius = CornerRadius(32f, 32f)

    val textPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = with(LocalDensity.current) { 24.sp.toPx() }
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.CENTER
    }
    val textBounds = Rect()

    Canvas(modifier = Modifier.aspectRatio(1f)) {
        val cellSize = Size(
            (size.width - totalPadding) / boardSize,
            (size.height - totalPadding) / boardSize
        )
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val cellPosition = Offset(
                    col * (padding + cellSize.width),
                    row * (padding + cellSize.height)
                )
                val value = board[Coordinate(col, row)]
                val cellColor = cellColors[value ?: 0] ?: cellColors[0]!!
                drawRoundRect(
                    color = cellColor,
                    topLeft = cellPosition,
                    size = cellSize,
                    cornerRadius = cornerRadius
                )
                val text = value?.toString()
                if (text != null) {
                    textPaint.getTextBounds(text, 0, text.length, textBounds)

                    // Calculate the actual displayed cell color to calculate the luminance
                    val blendedColor = ColorUtils.blendARGB(surfaceColor.toArgb(),
                        cellColor.toArgb(),
                        cellColor.alpha)

                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            text,
                            cellPosition.x + cellSize.width / 2,
                            cellPosition.y + cellSize.height / 2 - textBounds.exactCenterY(),
                            textPaint.apply {
                                color = android.graphics.Color.parseColor(
                                    if (blendedColor.luminance < 0.55) "#E6E1E5" else "#1C1B1F"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}