package debts.details.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import net.thebix.debts.R

class DetailsAvatarBehavior(
    context: Context,
    attrs: AttributeSet
) : CoordinatorLayout.Behavior<View>(context, attrs) {

    private val marginStart: Int = context.resources.getDimensionPixelOffset(net.thebix.debts.core.resource.R.dimen.margin_16dp)
    private val marginTop: Int = context.resources.getDimensionPixelOffset(net.thebix.debts.core.resource.R.dimen.margin_16dp)
    private val marginVerticalEnd: Int =
        context.resources.getDimensionPixelOffset(net.thebix.debts.core.resource.R.dimen.margin_12dp)

    private var startX: Int = -1
    private var startY: Int = -1
    private var endX: Int = -1
    private var endY: Int = -1
    private var startWidth: Int = -1
    private var endWidth: Int = -1
    private var startHeight: Int = -1
    private var endHeight: Int = -1
    private var maxScroll = -1

    private var toolbarHeight: Int = 0
    private var historyTitleContainerHeight: Int = 0

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean =
        dependency is RecyclerView

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        initialize(parent, child, dependency)

        val dependencyCalculatedY = dependency.y - toolbarHeight - historyTitleContainerHeight
        val expandedPercentageFactor: Float = dependencyCalculatedY / maxScroll
        val percentageMultiplier = (1f - expandedPercentageFactor)

        val xToSubtract = (startX - endX) * percentageMultiplier
        val yToSubtract = (startY - endY) * percentageMultiplier
        val widthToSubtract = ((startWidth - endWidth) * percentageMultiplier).toInt()
        val heightToSubtract = ((startHeight - endHeight) * percentageMultiplier).toInt()

        child.x = startX - xToSubtract
        child.y = startY - yToSubtract

        child.layoutParams = child.layoutParams.apply {
            width = (startWidth - widthToSubtract)
            height = (startHeight - heightToSubtract)
        }
        return true
    }

    private fun initialize(parent: ViewGroup, child: View, dependency: View) {
        if (toolbarHeight == 0) {
            val toolbarView = parent.findViewById(R.id.details_toolbar) as Toolbar
            val historyContainerView = parent.findViewById(R.id.details_history_container) as View

            toolbarHeight = toolbarView.height
            historyTitleContainerHeight = historyContainerView.height

            endX = toolbarView.children.firstOrNull()?.width ?: 0
            endHeight = toolbarHeight - marginVerticalEnd * 2
            endY = (toolbarView.y + (toolbarHeight - endHeight) / 2).toInt()
        }

        val newMaxScroll = dependency.y.toInt() - toolbarHeight - historyTitleContainerHeight
        if (newMaxScroll <= maxScroll) return

        startHeight = child.height
        startWidth = child.width
        endWidth = endHeight

        startX = marginStart
        startY = toolbarHeight + marginTop

        maxScroll = newMaxScroll
    }
}
