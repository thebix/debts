package debts.core.common.android.extensions

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

fun <T : View> Fragment.findViewById(@IdRes viewId: Int): T = context!!.tryToFindActivity()!!.findViewById(viewId)
