package debts.feature.details

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

private const val SNAP_THRESHOLD = 0.5f

/**
 * How far the Details header is collapsed: 0 is fully expanded, 1 is fully collapsed.
 *
 * Replaces `AppBarLayout` with `scroll|exitUntilCollapsed|snap`: scrolling the list up collapses
 * the header first, scrolling down expands it only once the list is at its top, and a gesture
 * that stops halfway settles on the nearest end.
 */
@Stable
internal class DetailsCollapseState(initialFraction: Float) {

    var fraction by mutableFloatStateOf(initialFraction)
        private set

    /** Height of the part of the header that scrolls away; reported by the header once measured. */
    var collapsibleHeightPx by mutableFloatStateOf(0f)

    val nestedScrollConnection = object : NestedScrollConnection {

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
            if (available.y < 0f) consume(available.y) else Offset.Zero

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
            if (available.y > 0f) consume(available.y) else Offset.Zero

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            settle()
            return Velocity.Zero
        }
    }

    private fun consume(deltaY: Float): Offset {
        if (collapsibleHeightPx <= 0f) return Offset.Zero
        val previous = fraction
        fraction = (previous - deltaY / collapsibleHeightPx).coerceIn(0f, 1f)
        return Offset(0f, (previous - fraction) * collapsibleHeightPx)
    }

    private suspend fun settle() {
        if (fraction == 0f || fraction == 1f) return
        animate(initialValue = fraction, targetValue = settledFraction()) { value, _ -> fraction = value }
    }

    private fun settledFraction() = if (fraction < SNAP_THRESHOLD) 0f else 1f

    companion object {

        // Saved already settled: a recreation in the middle of the snap animation must not freeze the header halfway.
        val Saver = Saver<DetailsCollapseState, Float>(
            save = { it.settledFraction() },
            restore = { DetailsCollapseState(it) },
        )
    }
}
