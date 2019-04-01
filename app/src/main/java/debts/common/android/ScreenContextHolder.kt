package debts.common.android

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import debts.common.android.extensions.isPermissionGranted
import debts.common.android.extensions.tryToFindActivity
import net.thebix.debts.BuildConfig
import java.lang.ref.WeakReference
import net.thebix.debts.R
import java.io.File
import java.io.FileOutputStream

interface ScreenContextHolder {

    companion object {

        const val FRAGMENT_DEBTORS = "FRAGMENT_DEBTORS"
        const val FRAGMENT_DETAILS = "FRAGMENT_DETAILS"
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
        chooserTitle: String,
        // TODO: change to Generic
        message: String
    )

    fun sendExplicitFile(
        chooserTitle: String,
        fileName: String,
        fileContent: String = "",
        fileMimeType: String = "text/plain"
    )

    enum class NavAnimation(val value: List<Int>) {

        FADE(listOf<Int>(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out))
    }

    // endregion

    // region Permissions

    fun isPermissionGranted(permission: String): Boolean

    fun requestPermissions(permissions: Array<String>, requestCode: Int)

    // endregion

    // region Notifications

    fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT)

    // endregion

    fun dispose()
}

class FragmentScreenContext(
    fragment: Fragment,
    private val fragmentRef: WeakReference<Fragment> = WeakReference(fragment)
) : ScreenContext {

    private val handler = Handler(Looper.getMainLooper())

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
        chooserTitle: String,
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
                val chooser: Intent = Intent.createChooser(sendIntent, chooserTitle)
                if (sendIntent.resolveActivity(packageManager) != null) {
                    fragment.startActivity(chooser)
                }
            }
        }
    }

    override fun sendExplicitFile(
        chooserTitle: String,
        fileName: String,
        fileContent: String,
        fileMimeType: String
    ) {
        fragmentRef.get()?.let { fragment ->
            (fragment.context?.tryToFindActivity())?.packageManager?.let { packageManager ->

                val applicationContext = fragment.context!!.applicationContext
                val folder = applicationContext.cacheDir.absolutePath + File.separator + "share"
                val subFolder = File(folder)
                if (!subFolder.exists()) {
                    subFolder.mkdirs()
                }
                val file = File(subFolder, fileName)
                val outputStream = FileOutputStream(file)
                outputStream.write(fileContent.toByteArray())
                outputStream.close()
                val uri = FileProvider.getUriForFile(
                    applicationContext,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    file
                )
                val intentShareFile = Intent(Intent.ACTION_SEND)
                intentShareFile.type = fileMimeType
                intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
                val chooser = Intent.createChooser(
                    intentShareFile,
                    chooserTitle
                )
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (intentShareFile.resolveActivity(packageManager) != null) {
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

    // region Notifications

    override fun showToast(text: String, duration: Int) {
        fragmentRef.get()?.let { fragment ->
            if (fragment.context != null && text.isNotBlank()) {
                handler.post {
                    Toast.makeText(fragment.context!!, text, duration)
                        .show()
                }
            }
        }
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
