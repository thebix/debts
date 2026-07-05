@file:Suppress("MagicNumber")

package debts.core.resource.theme

import androidx.compose.ui.graphics.Color

// Brand colors from the original View-based theme (colors.xml).
// Preserved here to enable quick palette rollback (option B at U2.4c checkpoint)
// and for visual comparison with the M3-generated scheme.
// In Views these were used as:
//   Primary    — toolbar background, ViewPager tab indicator background
//   PrimaryDark — status bar color, navigation bar color, window background
//   Accent     — FAB background, accent button background, tab selected indicator
object LegacyBrandPalette {
    val Primary = Color(0xFF84DEE9)
    val PrimaryDark = Color(0xFF34C7D9)
    val Accent = Color(0xFFFC4582)
}
