package debts.common.android

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import debts.details.DetailsActivity
import debts.preferences.PreferencesActivity
import io.reactivex.Completable
import io.reactivex.Single

class DebtsNavigator(
    private val screenContextHolder: ScreenContextHolder,
    private val applicationContext: Context,
    private val name: String
) {

    // region App navigation

    fun openDetails(debtorId: Long): Completable =
        Completable.fromCallable {
            screenContextHolder.get(name)?.openActivity(DetailsActivity.createIntent(applicationContext, debtorId))
        }

    fun openSettings(): Completable =
        Completable.fromCallable {
            screenContextHolder.get(name)?.openActivity(
                PreferencesActivity.createIntent(applicationContext)
            )
        }

    // endregion

    // region Share

    fun sendExplicit(
        chooserTitle: String,
        // TODO: change to Generic
        message: String
    ): Completable =
        Completable.fromCallable {
            screenContextHolder.get(name)?.sendExplicit(
                chooserTitle,
                message
            )
        }

    fun sendExplicitFile(
        chooserTitle: String,
        fileName: String,
        fileContent: String,
        fileMimeType: String
    ): Completable =
        Completable.fromCallable {
            screenContextHolder.get(name)?.sendExplicitFile(
                chooserTitle,
                fileName,
                fileContent,
                fileMimeType
            )
        }

    // endregion

    // region Permissions

    fun isPermissionGranted(permission: String): Single<Boolean> =
        Single.fromCallable {
            screenContextHolder.get(name)?.isPermissionGranted(permission) ?: false
        }

    fun requestPermission(permission: String, requestCode: Int): Completable =
        Completable.fromCallable {
            screenContextHolder.get(name)?.requestPermissions(arrayOf(permission), requestCode)
        }

    // endregion

    // region Notifications

    fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
        screenContextHolder.get(name)?.showToast(text, duration)
    }

    fun showToast(@StringRes textId: Int, duration: Int = Toast.LENGTH_SHORT) {
        showToast(applicationContext.getString(textId), duration)
    }

    // endregion
}
