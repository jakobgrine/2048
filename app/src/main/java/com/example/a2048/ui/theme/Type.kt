package com.example.a2048.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.a2048.R

private val pressStart2PFamily = FontFamily(
    Font(R.font.pressstart2p_regular, FontWeight.Normal),
)

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontSize = 24.sp
    ),
    button = TextStyle(
        fontSize = 24.sp
    ),
    subtitle1 = TextStyle(
        fontSize = 20.sp
    ),
    defaultFontFamily = FontFamily.Monospace,
    // defaultFontFamily = pressStart2PFamily,
)