package okb.common.android.extension

import androidx.annotation.LayoutRes
import android.view.View
import android.view.ViewGroup

// region Helpers
///////////////////////////////////////////////////////////////////////////

fun ViewGroup.selfInflate(@LayoutRes resId: Int) {
    View.inflate(context, resId, this)
}

// endregion


// region Children
///////////////////////////////////////////////////////////////////////////

operator fun ViewGroup.contains(view: View): Boolean = indexOfChild(view) != -1

// endregion
