package debts.common.android

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import debts.core.common.android.BaseActivity
import debts.core.common.android.extensions.isPermissionGranted
import debts.core.common.android.extensions.tryToFindActivity
import debts.core.common.android.navigation.ScreenContext
import debts.core.common.android.navigation.ScreenContextHolder
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

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

class FragmentScreenContext(
    fragment: Fragment,
    private val fragmentRef: WeakReference<Fragment> = WeakReference(fragment),
    private val applicationId: String,
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
            replaceFragment(rootId, fragment, addToBackStack, animation, activity as BaseActivity)
        }
    }

    override fun addFragment(
        rootId: Int,
        fragment: Fragment,
        addToBackStack: Boolean,
        animation: ScreenContext.NavAnimation
    ) {
        fragmentRef.get()?.context?.tryToFindActivity()?.let { activity ->
            addFragment(rootId, fragment, addToBackStack, animation, activity as BaseActivity)
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
            (fragment.context?.tryToFindActivity())?.let { activity ->
                sendExplicit(chooserTitle, message, activity as BaseActivity)
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
            (fragment.context?.tryToFindActivity())?.let { activity ->
                sendExplicitFile(
                    chooserTitle,
                    fileName,
                    fileContent,
                    fileMimeType,
                    activity as BaseActivity,
                    applicationId,
                )
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
                    Toast.makeText(fragment.requireContext(), text, duration)
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

class ActivityScreenContext(
    activity: BaseActivity,
    private val activityRef: WeakReference<BaseActivity> = WeakReference(activity),
    private val applicationId: String,
) : ScreenContext {

    private val handler = Handler(Looper.getMainLooper())

    // region Navigation

    override fun replaceFragment(
        rootId: Int,
        fragment: Fragment,
        addToBackStack: Boolean,
        animation: ScreenContext.NavAnimation
    ) {
        activityRef.get()?.let { activity ->
            replaceFragment(rootId, fragment, addToBackStack, animation, activity)
        }
    }

    override fun addFragment(
        rootId: Int,
        fragment: Fragment,
        addToBackStack: Boolean,
        animation: ScreenContext.NavAnimation
    ) {
        activityRef.get()?.let { activity ->
            addFragment(rootId, fragment, addToBackStack, animation, activity)
        }
    }

    override fun openActivity(intent: Intent) {
        activityRef.get()?.startActivity(intent)
    }

    override fun sendExplicit(
        chooserTitle: String,
        message: String
    ) {
        activityRef.get()?.let { activity ->
            sendExplicit(chooserTitle, message, activity)
        }
    }

    override fun sendExplicitFile(
        chooserTitle: String,
        fileName: String,
        fileContent: String,
        fileMimeType: String
    ) {
        activityRef.get()?.let { activity ->
            sendExplicitFile(
                chooserTitle,
                fileName,
                fileContent,
                fileMimeType,
                activity,
                applicationId,
            )
        }
    }

    // endregion

    // region Permissions

    override fun isPermissionGranted(permission: String): Boolean {
        return activityRef.get()?.isPermissionGranted(permission) ?: false
    }

    override fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        activityRef.get()?.let { activity ->
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

    // endregion

    // region Notifications

    override fun showToast(text: String, duration: Int) {
        activityRef.get()?.let { activity ->
            if (text.isNotBlank()) {
                handler.post {
                    Toast.makeText(activity.applicationContext, text, duration)
                        .show()
                }
            }
        }
    }

    // endregion

    override fun dispose() {
        // TODO: I'm not sure it's necessary
        activityRef.clear()
    }
}

// region Common functions

private fun sendExplicit(
    chooserTitle: String,
    message: String,
    activity: BaseActivity
) {
    activity.packageManager?.let { packageManager ->
        val sendIntent = Intent(Intent.ACTION_SEND)
            .apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }
        val chooser: Intent = Intent.createChooser(sendIntent, chooserTitle)
        if (sendIntent.resolveActivity(packageManager) != null) {
            activity.startActivity(chooser)
        }
    }
}

private fun sendExplicitFile(
    chooserTitle: String,
    fileName: String,
    fileContent: String,
    fileMimeType: String,
    activity: BaseActivity,
    applicationId: String,
) {
    activity.packageManager?.let { packageManager ->

        val applicationContext = activity.applicationContext
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
            "${applicationId}.fileprovider",
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
            activity.startActivity(chooser)
        }
    }
}

private fun replaceFragment(
    rootId: Int,
    fragment: Fragment,
    addToBackStack: Boolean,
    animation: ScreenContext.NavAnimation,
    activity: BaseActivity
) {
    activity.replaceFragment(fragment, rootId, addToBackStack, animation.value)
}

private fun addFragment(
    rootId: Int,
    fragment: Fragment,
    addToBackStack: Boolean,
    animation: ScreenContext.NavAnimation,
    activity: BaseActivity
) {
    activity.addFragment(fragment, rootId, addToBackStack, animation.value)
}

// endregion
