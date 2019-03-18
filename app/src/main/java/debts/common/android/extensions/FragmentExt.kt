package debts.common.android.extensions

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import okb.common.android.extension.tryToFindActivity

fun <T : View> Fragment.findViewById(@IdRes viewId: Int): T = context!!.tryToFindActivity()!!.findViewById(viewId)
