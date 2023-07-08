package debts.core.common.android

import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    override fun onDestroy() {
        super.onDestroy()

        if (requireActivity().isFinishing) {
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
}
