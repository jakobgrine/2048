package com.example.a2048.ui.game

import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp

@Composable
fun Board(board: BoardState, boardSize: Int) {
    val padding = 24f
    val totalPadding = (boardSize - 1) * padding

    val cellColors = mapOf(
        0 to MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
        2 to Color(0xFFEEE4DA),
        4 to Color(0xFFEEE1C9),
        8 to Color(0xFFF3B27A),
        16 to Color(0xFFF69664),
        32 to Color(0xFFF77C5F),
        64 to Color(0xFFF75F3B),
        128 to Color(0xFFEDD073),
        256 to Color(0xFFEDCC62),
        512 to Color(0xFFEDC950),
        1024 to Color(0xFFEDC53F),
        2048 to Color(0xFFEDC22E),
    )
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
                drawRoundRect(
                    color = cellColors[value ?: 0] ?: cellColors[0]!!,
                    topLeft = cellPosition,
                    size = cellSize,
                    cornerRadius = cornerRadius
                )
                val text = value?.toString()
                if (text != null) {
                    textPaint.getTextBounds(text, 0, text.length, textBounds)
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            text,
                            cellPosition.x + cellSize.width / 2,
                            cellPosition.y + cellSize.height / 2 - textBounds.exactCenterY(),
                            textPaint.apply {
                                color =
                                    if (value >= 8)
                                        android.graphics.Color.parseColor("#F9F6F2")
                                    else
                                        android.graphics.Color.parseColor("#776E65")
                            }
                        )
                    }
                }
            }
        }
    }
}