package debts.core.resource.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Fills the area under the system navigation bar (the gesture handle) with the app bar colour,
 * as the window background did before Compose; otherwise the screen surface shows through it.
 * Pass it as the `bottomBar` of a screen's `Scaffold`.
 */
@Composable
fun NavigationBarBackground(color: Color = MaterialTheme.colorScheme.primary) {
    Spacer(
        Modifier
            .fillMaxWidth()
            .windowInsetsBottomHeight(WindowInsets.navigationBars)
            .background(color)
    )
}
