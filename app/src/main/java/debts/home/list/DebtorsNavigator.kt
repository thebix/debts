package debts.home.list

import debts.home.ScreenContextHolder
import debts.home.details.DetailsFragment
import io.reactivex.Completable
import io.reactivex.Single
import net.thebix.debts.R

class DebtorsNavigator(
    private val screenContextHolder: ScreenContextHolder
) {

    fun openDetails(debtorId: Long): Completable =
        Completable.fromCallable {
            screenContextHolder.get(ScreenContextHolder.FRAGMENT_DEBTORS)?.replaceFragment(
                R.id.home_root,
                DetailsFragment.createInstance(debtorId)
            )
        }

    fun isPermissionGranted(permission: String): Single<Boolean> =
        Single.fromCallable {
            screenContextHolder.get(ScreenContextHolder.FRAGMENT_DEBTORS)?.isPermissionGranted(permission) ?: false
        }

    fun requestPermission(permission: String, requestCode: Int): Completable =
        Completable.fromCallable {
            screenContextHolder.get(ScreenContextHolder.FRAGMENT_DEBTORS)?.requestPermissions(
                arrayOf(permission), requestCode
            )
        }
}
