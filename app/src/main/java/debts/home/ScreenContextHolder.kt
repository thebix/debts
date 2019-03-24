package debts.home

import android.content.Intent
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import debts.common.android.BaseActivity
import debts.common.android.BaseFragment
import debts.common.android.extensions.tryToFindActivity
import java.lang.ref.WeakReference
import net.thebix.debts.R

interface ScreenContextHolder {

    companion object {

        const val FRAGMENT_DEBTORS = "FRAGMENT_DEBTORS"
    }

    fun set(screenKey: String, contextHolder: ScreenContext)
    fun get(screenKey: String): ScreenContext?
    fun remove(screenKey: String)
}

class ScreenContextHolderImpl : ScreenContextHolder {

    private val screens: MutableMap<String, ScreenContext> = mutableMapOf()

    override fun set(screenKey: String, contextHolder: ScreenContext) {
        screens[screenKey] = contextHolder
    }

    override fun get(screenKey: String): ScreenContext? = screens[screenKey]

    override fun remove(screenKey: String) {
        screens[screenKey]?.let { screen ->
            screen.dispose()
        }
        screens.remove(screenKey)
    }
}

interface ScreenContext {

    // region Navigation

    fun replaceFragment(
        @IdRes rootId: Int,
        fragment: Fragment,
        addToBackStack: Boolean = true,
        animation: ScreenContext.NavAnimation = NavAnimation.FADE
    )

    fun addFragment(
        @IdRes rootId: Int,
        fragment: Fragment,
        addToBackStack: Boolean = true,
        animation: ScreenContext.NavAnimation = NavAnimation.FADE
    )

    fun sendExplicit(
        @StringRes chooserTitleId: Int = 0,
        // TODO: change to Generic
        message: String
    )

    fun dispose()

    enum class NavAnimation(val value: List<Int>) {
        FADE(listOf<Int>(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out))
    }

    // endregion
}

// INFO: rename it to ActivityScreenContext and create a new class FragmentScreenContext if fragment context be needed
class FragmentScreenContext(
    activity: BaseActivity,
    private val activityRef: WeakReference<BaseActivity> = WeakReference(activity)
) : ScreenContext {

    constructor(fragment: BaseFragment) : this(fragment.context?.tryToFindActivity() as BaseActivity)

    override fun replaceFragment(
        rootId: Int,
        fragment: Fragment,
        addToBackStack: Boolean,
        animation: ScreenContext.NavAnimation
    ) {
        activityRef.get()?.let { activity ->
            activity.replaceFragment(
                fragment = fragment,
                rootId = rootId,
                addToBackStack = addToBackStack,
                animations = animation.value
            )
        }
    }

    override fun addFragment(
        rootId: Int,
        fragment: Fragment,
        addToBackStack: Boolean,
        animation: ScreenContext.NavAnimation
    ) {
        activityRef.get()?.let { activity ->
            activity.addFragment(
                fragment = fragment,
                rootId = rootId,
                addToBackStack = addToBackStack,
                animations = animation.value
            )
        }
    }

    override fun sendExplicit(
        chooserTitleId: Int,
        message: String
    ) {
        activityRef.get()?.let { activity ->
            val sendIntent = Intent(Intent.ACTION_SEND)
                .apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, message)
                    type = "text/plain"
                }

            val title: String = activity.getString(chooserTitleId)
            val chooser: Intent = Intent.createChooser(sendIntent, title)
            if (sendIntent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(chooser)
            }
        }
    }

    override fun dispose() {
        // TODO: I'm not sure it's necessary
        activityRef.clear()
    }
}
