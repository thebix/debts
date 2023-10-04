package debts.common.android

import android.content.Context
import androidx.annotation.StringRes
import debts.core.common.android.navigation.DebtsNavigator
import debts.core.common.android.navigation.ScreenContextHolder
import debts.feature.details.DetailsActivity
import debts.feature.preferences.PreferencesActivity
import io.reactivex.Completable
import io.reactivex.Single

class DebtsNavigatorImpl(
    private val screenContextHolder: ScreenContextHolder,
    private val applicationContext: Context,
    private val name: String
) : DebtsNavigator {

    // region App navigation

    override fun openDetails(debtorId: Long): Completable =
        Completable.fromCallable {
            screenContextHolder.get(name)?.openActivity(DetailsActivity.createIntent(applicationContext, debtorId))
        }

    override fun openSettings(): Completable =
        Completable.fromCallable {
            screenContextHolder.get(name)?.openActivity(
                PreferencesActivity.createIntent(applicationContext)
            )
        }

    // endregion

    // region Share

    override fun sendExplicit(
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

    override fun sendExplicitFile(
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

    override fun isPermissionGranted(permission: String): Single<Boolean> =
        Single.fromCallable {
            screenContextHolder.get(name)?.isPermissionGranted(permission) ?: false
        }

    override fun requestPermission(permission: String, requestCode: Int): Completable =
        Completable.fromCallable {
            screenContextHolder.get(name)?.requestPermissions(arrayOf(permission), requestCode)
        }

    // endregion

    // region Notifications

    override fun showToast(text: String, duration: Int) {
        screenContextHolder.get(name)?.showToast(text, duration)
    }

    override fun showToast(@StringRes textId: Int, duration: Int) {
        showToast(applicationContext.getString(textId), duration)
    }

    // endregion
}
