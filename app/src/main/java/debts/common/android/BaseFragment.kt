package debts.common.android

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

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

    fun replaceFragment(
        fragment: androidx.fragment.app.Fragment, @IdRes rootId: Int,
        addToBackStack: Boolean = true,
        animations: List<Int> = listOf()
    ) {
        if (activity != null) {
            (activity as BaseActivity).replaceFragment(fragment, rootId, addToBackStack, animations)
        }
    }

    fun addFragment(
        fragment: androidx.fragment.app.Fragment, @IdRes rootId: Int,
        addToBackStack: Boolean = true,
        animations: List<Int> = listOf()
    ) {
        if (activity != null) {
            (activity as BaseActivity).addFragment(fragment, rootId, addToBackStack, animations)
        }
    }
}
