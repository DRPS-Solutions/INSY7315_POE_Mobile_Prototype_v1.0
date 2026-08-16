package com.qulloobul.moemienen.ui.theme

import androidx.compose.ui.graphics.Color

// Pulled directly from MainLayout.razor's MudTheme PaletteLight/PaletteDark,
// plus the --drps-* CSS variables reused across Home.razor / Tasks.razor.

// Light palette
val LightPrimary = Color(0xFF3C7A63)
val LightSecondary = Color(0xFF2F5C4C)
val LightInfo = Color(0xFF2A5CD1)
val LightAppBar = Color(0xFF12271F)
val LightBackground = Color(0xFFF5F3EC)
val LightSurface = Color(0xFFFFFFFF)
val LightTextMuted = Color(0xFF75806F)
val LightBorderSoft = Color(0xFFE7E4D8)

// Dark palette
val DarkPrimary = Color(0xFF6FA98C)
val DarkSecondary = Color(0xFF3C7A63)
val DarkInfo = Color(0xFF5B84D6)
val DarkAppBar = Color(0xFF12271F)
val DarkBackground = Color(0xFF0B1B21)
val DarkSurface = Color(0xFF1E362B)
val DarkTextPrimary = Color(0xFFE9F0EA)
val DarkTextMuted = Color(0xFF93A69A)
val DarkBorderSoft = Color(0xFF2E4A3B)

// Shared accents (priority colors, gradients)
val NavyGradientStart = Color(0xFF12271F)
val NavyGradientEnd = Color(0xFF2F5C4C)
val BlueGradientStart = Color(0xFF6FA98C)
val BlueGradientEnd = Color(0xFF3C7A63)

val PriorityHigh = Color(0xFFE0435C)
val PriorityMedium = Color(0xFFB9862E)
val PriorityLow = Color(0xFF3C7A63)

val TintGreen = Color(0xFFE7F3EB)
val TintGreenBorder = Color(0xFFA9CDB8)
val TintBlue = Color(0xFFEAF2ED)
val TintAmber = Color(0xFFF6EBD3)
val TintRed = Color(0xFFFCEEF0)

// Dedicated accent blue (kept separate from the primary green palette),
// used only for "linked to event/task" reference chips - matches the
// --drps-accent-blue / --drps-tint-accent-blue CSS vars in Requests.razor
// / Notifications.razor.
val AccentBlue = Color(0xFF2A5CD1)
val TintAccentBlue = Color(0xFFEEF2FC)

val StatusPending = Color(0xFFB9862E)
val StatusApproved = Color(0xFF2E9E5B)
val StatusDenied = Color(0xFFE0435C)

val Color_White = Color(0xFFFFFFFF)
val Color_Navy = Color(0xFF1C3D32)
