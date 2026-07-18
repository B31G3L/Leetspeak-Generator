package com.beigel.leetSpeak_Generator.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Redesign v4: Karten-Radius 14-16dp, Chips/Pills/Buttons 10-14dp oder voll rund (100dp)
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),   // kleine Chips / Badges
    small = RoundedCornerShape(14.dp),        // Listen-Karten (Modi), Textfelder
    medium = RoundedCornerShape(16.dp),       // Haupt-Karten (Input/Output, Erstellen)
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)    // Onboarding Icon-Kachel
)

/** Voll gerundete Form für Pills, Filter-Chips, Buttons und die schwebende Bottom-Nav. */
val PillShape = RoundedCornerShape(100.dp)
