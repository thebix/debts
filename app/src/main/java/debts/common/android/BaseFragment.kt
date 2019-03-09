package debts.common.android

import android.app.Activity
import android.support.annotation.CallSuper
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.view.View
import android.view.inputmethod.InputMethodManager

abstract class BaseFragment : Fragment() {

    override fun onDestroy() {
        super.onDestroy()

        if (activity!!.isFinishing) {
            onScopeFinished()
            return
        }

        if (isStateSaved) {
            return
        }

        var anyParentIsRemoving = false

        // region Api >= 17

        var parent = parentFragment
        while (!anyParentIsRemoving && parent != null) {
            anyParentIsRemoving = parent.isRemoving
            parent = parent.parentFragment
        }

        // endregion

        if (isRemoving || anyParentIsRemoving) {
            onScopeFinished()
        }

    }

    @CallSuper
    protected open fun onScopeFinished() {
        // implement in children
    }

    protected fun closeKeyboard() {
        activity?.let {
            val imm = it.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = it.currentFocus
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun replaceFragment(fragment: Fragment, @IdRes rootId: Int, addToBackStack: Boolean = true) {
        if (activity != null) {
            (activity as BaseActivity).replaceFragment(fragment, rootId, addToBackStack)
        }
    }

    fun addFragment(fragment: Fragment, @IdRes rootId: Int, addToBackStack: Boolean = true) {
        if (activity != null) {
            (activity as BaseActivity).addFragment(fragment, rootId, addToBackStack)
        }
    }
}
