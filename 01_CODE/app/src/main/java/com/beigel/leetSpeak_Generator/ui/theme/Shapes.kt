package com.beigel.leetSpeak_Generator.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),    // ShapeAppearance.App.SmallComponent
    small = RoundedCornerShape(8.dp),         // ShapeAppearance.App.MediumComponent
    medium = RoundedCornerShape(12.dp),       // ShapeAppearance.App.LargeComponent
    large = RoundedCornerShape(16.dp),        // Für größere Komponenten
    extraLarge = RoundedCornerShape(20.dp)    // Für sehr große Komponenten
)