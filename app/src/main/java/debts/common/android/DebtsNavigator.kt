package debts.common.android

import android.content.Context
import debts.home.details.DetailsFragment
import debts.preferences.PreferencesActivity
import io.reactivex.Completable
import io.reactivex.Single
import net.thebix.debts.R

class DebtsNavigator(
    private val screenContextHolder: ScreenContextHolder,
    private val applicationContext: Context,
    private val name: String
) {

    // region App navigation

    fun openDetails(debtorId: Long): Completable =
        Completable.fromCallable {
            screenContextHolder.get(name)?.replaceFragment(
                R.id.home_root,
                DetailsFragment.createInstance(debtorId)
            )
        }

    fun openSettings(): Completable =
        Completable.fromCallable {
            screenContextHolder.get(name)?.openActivity(
                PreferencesActivity.createIntent(applicationContext)
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
            screenContextHolder.get(name)?.requestPermissions(
                arrayOf(permission), requestCode
            )
        }

    // endregion
}
