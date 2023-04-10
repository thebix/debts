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

class DetailsTitleBehavior(
    context: Context,
    attrs: AttributeSet,
) : CoordinatorLayout.Behavior<View>(context, attrs) {

    private val marginStart: Int = context.resources.getDimensionPixelOffset(R.dimen.margin_16dp)
    private val marginEnd: Int = context.resources.getDimensionPixelOffset(R.dimen.margin_16dp)
    private val marginTop: Int =
        context.resources.getDimensionPixelOffset(R.dimen.margin_74dp)

    private var startX: Int = -1
    private var startY: Int = -1
    private var endX: Int = -1
    private var endY: Int = -1
    private var startWidth: Int = -1
    private var endWidth: Int = -1
    private var startHeight: Int = -1
    private var endHeight: Int = -1
    private var startScale: Int = 1

    @Suppress("MagicNumber")
    private var endScale: Float = 0.7f
    private var maxScroll = -1
    private var menuFirstItemX = -1

    private var toolbarHeight: Int = 0
    private var historyTitleContainerHeight: Int = 0

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View,
    ): Boolean =
        dependency is RecyclerView

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View,
    ): Boolean {
        initialize(parent, child, dependency)

        val dependencyCalculatedY = dependency.y - toolbarHeight - historyTitleContainerHeight
        val expandedPercentageFactor: Float = dependencyCalculatedY / maxScroll
        val percentageMultiplier = (1f - expandedPercentageFactor)

        val xToSubtract = (startX - endX) * percentageMultiplier
        val yToSubtract = (startY - endY) * percentageMultiplier
        val widthToSubtract = ((startWidth - endWidth) * percentageMultiplier).toInt()
        val heightToSubtract = ((startHeight - endHeight) * percentageMultiplier).toInt()

        val scaleToSubtract = (startScale - endScale) * percentageMultiplier
        child.scaleY = startScale - scaleToSubtract
        child.scaleX = startScale - scaleToSubtract

        child.x = startX - xToSubtract
        child.y = startY - yToSubtract
        child.layoutParams = child.layoutParams.apply {
            width = (startWidth - widthToSubtract)
            height = (startHeight - heightToSubtract)
        }
        return true
    }

    private fun initialize(parent: ViewGroup, child: View, dependency: View) {
        if (menuFirstItemX != -1) return

        val toolbarView = parent.findViewById(R.id.details_toolbar) as Toolbar
        val menuDeleteView = toolbarView.findViewById<View>(R.id.home_details_menu_delete)
        if (menuDeleteView != null) {
            val itemWindowLocation = IntArray(2)
            menuDeleteView.getLocationInWindow(itemWindowLocation)
            menuFirstItemX = itemWindowLocation[0]
            endWidth = ((menuFirstItemX - startX) * (1 + (1f - endScale))).toInt()
        }

        val historyContainerView = parent.findViewById(R.id.details_history_container) as View
        val avatarView = parent.findViewById(R.id.details_avatar) as View

        toolbarHeight = toolbarView.height
        historyTitleContainerHeight = historyContainerView.height

        val newMaxScroll = dependency.y.toInt() - toolbarHeight - historyTitleContainerHeight
        if (newMaxScroll <= maxScroll) return

        startHeight = child.height
        endHeight = child.height

        startX = marginStart + avatarView.width + marginStart
        @Suppress("MagicNumber")
        endX = (toolbarView.children.firstOrNull()?.width ?: 0) + 4
        startY = toolbarHeight + (marginTop - child.height) / 2
        endY = (toolbarHeight - endHeight) / 2

        startWidth = toolbarView.measuredWidth - startX - marginEnd

        maxScroll = dependency.y.toInt() - toolbarHeight - historyTitleContainerHeight
    }
}
