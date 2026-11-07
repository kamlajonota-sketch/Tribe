package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val TribeShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),   // Cards, text fields, chips
    large = RoundedCornerShape(24.dp),    // Bottom sheets, hero banners
    extraLarge = RoundedCornerShape(32.dp) // Modals, dialogue cards
)

val PillShape = RoundedCornerShape(100.dp)
val CardShape = RoundedCornerShape(16.dp)
val BannerShape = RoundedCornerShape(20.dp)
val ButtonShape = RoundedCornerShape(12.dp)
