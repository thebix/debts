package debts.common.android

import android.content.Intent
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import debts.common.android.extensions.isPermissionGranted
import debts.common.android.extensions.tryToFindActivity
import java.lang.ref.WeakReference
import net.thebix.debts.R

interface ScreenContextHolder {

    companion object {

        const val FRAGMENT_DEBTORS = "FRAGMENT_DEBTORS"
        const val FRAGMENT_MAIN_PREFERENCES = "FRAGMENT_MAIN_PREFERENCES"
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
        animation: NavAnimation = NavAnimation.FADE
    )

    fun addFragment(
        @IdRes rootId: Int,
        fragment: Fragment,
        addToBackStack: Boolean = true,
        animation: NavAnimation = NavAnimation.FADE
    )

    fun openActivity(intent: Intent)

    fun sendExplicit(
        @StringRes chooserTitleId: Int = 0,
        // TODO: change to Generic
        message: String
    )

    enum class NavAnimation(val value: List<Int>) {

        FADE(listOf<Int>(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out))
    }

    // endregion

    // region Permissions

    fun isPermissionGranted(permission: String): Boolean

    fun requestPermissions(permissions: Array<String>, requestCode: Int)

    // endregion

    fun dispose()
}

class FragmentScreenContext(
    fragment: Fragment,
    private val fragmentRef: WeakReference<Fragment> = WeakReference(fragment)
) : ScreenContext {

    // region Navigation

    override fun replaceFragment(
        rootId: Int,
        fragment: Fragment,
        addToBackStack: Boolean,
        animation: ScreenContext.NavAnimation
    ) {
        fragmentRef.get()?.context?.tryToFindActivity()?.let { activity ->
            (activity as BaseActivity).replaceFragment(fragment, rootId, addToBackStack, animation.value)
        }
    }

    override fun addFragment(
        rootId: Int,
        fragment: Fragment,
        addToBackStack: Boolean,
        animation: ScreenContext.NavAnimation
    ) {
        fragmentRef.get()?.context?.tryToFindActivity()?.let { activity ->
            (activity as BaseActivity).addFragment(fragment, rootId, addToBackStack, animation.value)
        }
    }

    override fun openActivity(intent: Intent) {
        fragmentRef.get()?.startActivity(intent)
    }

    override fun sendExplicit(
        chooserTitleId: Int,
        message: String
    ) {
        fragmentRef.get()?.let { fragment ->
            (fragment.context?.tryToFindActivity())?.packageManager?.let { packageManager ->
                val sendIntent = Intent(Intent.ACTION_SEND)
                    .apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, message)
                        type = "text/plain"
                    }
                val title: String = fragment.getString(chooserTitleId)
                val chooser: Intent = Intent.createChooser(sendIntent, title)
                if (sendIntent.resolveActivity(packageManager) != null) {
                    fragment.startActivity(chooser)
                }
            }
        }
    }

    // endregion

    // region Permissions

    override fun isPermissionGranted(permission: String): Boolean {
        return fragmentRef.get()?.context?.isPermissionGranted(permission) ?: false
    }

    override fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        fragmentRef.get()?.requestPermissions(permissions, requestCode)
    }

    // endregion

    override fun dispose() {
        // TODO: I'm not sure it's necessary
        fragmentRef.clear()
    }
}

// region ActivityScreenContext. Uncomment and update when ActivityContext is needed

//class ActivityScreenContext(
//    activity: BaseActivity,
//    private val activityRef: WeakReference<BaseActivity> = WeakReference(activity)
//) : ScreenContext {
//
//    constructor(fragment: BaseFragment) : this(fragment.context?.tryToFindActivity() as BaseActivity)
//
//    override fun replaceFragment(
//        rootId: Int,
//        fragment: Fragment,
//        addToBackStack: Boolean,
//        animation: ScreenContext.NavAnimation
//    ) {
//        activityRef.get()?.replaceFragment(
//            fragment = fragment,
//            rootId = rootId,
//            addToBackStack = addToBackStack,
//            animations = animation.value
//        )
//    }
//
//    override fun addFragment(
//        rootId: Int,
//        fragment: Fragment,
//        addToBackStack: Boolean,
//        animation: ScreenContext.NavAnimation
//    ) {
//        activityRef.get()?.addFragment(
//            fragment = fragment,
//            rootId = rootId,
//            addToBackStack = addToBackStack,
//            animations = animation.value
//        )
//    }
//
//    override fun sendExplicit(
//        chooserTitleId: Int,
//        message: String
//    ) {
//        activityRef.get()?.let { activity ->
//            val sendIntent = Intent(Intent.ACTION_SEND)
//                .apply {
//                    action = Intent.ACTION_SEND
//                    putExtra(Intent.EXTRA_TEXT, message)
//                    type = "text/plain"
//                }
//
//            val title: String = activity.getString(chooserTitleId)
//            val chooser: Intent = Intent.createChooser(sendIntent, title)
//            if (sendIntent.resolveActivity(activity.packageManager) != null) {
//                activity.startActivity(chooser)
//            }
//        }
//    }
//
//    override fun isPermissionGranted(permission: String): Boolean {
//        return activityRef.get()?.isPermissionGranted(permission) ?: false
//    }
//
//    override fun requestPermissions(permissions: Array<String>, requestCode: Int) {
//        activityRef.get()?.let { activity ->
//            ActivityCompat.requestPermissions(activity, permissions, requestCode)
//        }
//    }
//
//    override fun dispose() {
//        // TODO: I'm not sure it's necessary
//        activityRef.clear()
//    }
//}

// endregion
