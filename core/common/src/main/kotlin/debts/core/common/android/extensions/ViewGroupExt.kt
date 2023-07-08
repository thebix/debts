package debts.core.common.android.extensions

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

// region Helpers

fun ViewGroup.selfInflate(@LayoutRes resId: Int) {
    View.inflate(context, resId, this)
}

// endregion

// region Children

operator fun ViewGroup.contains(view: View): Boolean = indexOfChild(view) != -1

// endregion
