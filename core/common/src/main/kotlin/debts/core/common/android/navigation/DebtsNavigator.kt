package debts.core.common.android.navigation

import android.widget.Toast
import androidx.annotation.StringRes
import io.reactivex.Completable
import io.reactivex.Single

interface DebtsNavigator {

    // region App navigation

    fun openDetails(debtorId: Long): Completable

    fun openSettings(): Completable

    // endregion

    // region Share

    fun sendExplicit(
        chooserTitle: String,
        // TODO: change to Generic
        message: String
    ): Completable

    fun sendExplicitFile(
        chooserTitle: String,
        fileName: String,
        fileContent: String,
        fileMimeType: String
    ): Completable

    // endregion

    // region Permissions

    fun isPermissionGranted(permission: String): Single<Boolean>

    fun requestPermission(permission: String, requestCode: Int): Completable

    // endregion

    // region Notifications

    fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT)

    fun showToast(@StringRes textId: Int, duration: Int = Toast.LENGTH_SHORT)

    // endregion
}
