package debts.core.common.android.navigation

import android.content.Intent
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

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

        FADE(
            listOf<Int>(
                net.thebix.debts.core.resource.R.anim.fade_in,
                net.thebix.debts.core.resource.R.anim.fade_out,
                net.thebix.debts.core.resource.R.anim.fade_in,
                net.thebix.debts.core.resource.R.anim.fade_out
            )
        )
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
