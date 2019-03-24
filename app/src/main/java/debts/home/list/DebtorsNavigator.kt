package debts.home.list

import debts.home.ScreenContextHolder
import debts.home.details.DetailsFragment
import io.reactivex.Completable
import net.thebix.debts.R

class DebtorsNavigator(
    private val screenContextHolder: ScreenContextHolder
) {

    fun openDetails(debtorId: Long) =
        Completable.fromCallable {
            screenContextHolder.get(ScreenContextHolder.FRAGMENT_DEBTORS)?.replaceFragment(
                R.id.home_root,
                DetailsFragment.createInstance(debtorId)
            )
        }
}
