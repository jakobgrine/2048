package com.example.a2048.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.a2048.R

// private val pressStart2PFamily = FontFamily(
//     Font(R.font.pressstart2p_regular, FontWeight.Normal),
// )

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontSize = 24.sp,
        fontFamily = FontFamily.Monospace,
    ),
    labelLarge = TextStyle(
        fontSize = 24.sp,
        fontFamily = FontFamily.Monospace,
    ),
    // defaultFontFamily = pressStart2PFamily,
)