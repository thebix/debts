package debts.home.list.mvi

import debts.core.common.android.mvi.MviViewModel
import debts.core.common.android.mvi.OneShot
import debts.home.list.adapter.toContactsItemViewModel
import debts.core.repository.SortType
import io.reactivex.functions.BiFunction

class HomeViewModel(
    interactor: HomeInteractor,
) : MviViewModel<HomeIntention, HomeAction, HomeResult, HomeState>(interactor) {

    override val defaultState: HomeState
        get() = HomeState()

    @Suppress("ComplexMethod")
    override fun actionFromIntention(intent: HomeIntention): HomeAction =
        when (intent) {
            is HomeIntention.Init -> HomeAction.Init(intent.contactPermission, intent.requestCode)
            HomeIntention.InitMenu -> HomeAction.InitMenu
            is HomeIntention.Filter -> HomeAction.Filter(intent.name)
            is HomeIntention.ToggleSortByAmount -> HomeAction.SortBy(
                when (intent.currentSortType) {
                    SortType.AMOUNT_ASC -> SortType.AMOUNT_DESC
                    SortType.AMOUNT_DESC -> SortType.NOTHING
                    else -> SortType.AMOUNT_ASC
                }
            )

            is HomeIntention.ToggleSortByName -> HomeAction.SortBy(
                when (intent.currentSortType) {
                    SortType.NAME_ASC -> SortType.NAME_DESC
                    SortType.NAME_DESC -> SortType.NOTHING
                    else -> SortType.NAME_ASC
                }
            )

            HomeIntention.OpenSettings -> HomeAction.OpenSettings
            is HomeIntention.ShareAllDebts -> HomeAction.ShareAllDebts(intent.titleText)
            is HomeIntention.AddDebt -> HomeAction.AddDebt(
                intent.contactId,
                intent.name,
                intent.amount,
                intent.comment,
                intent.date
            )

            is HomeIntention.OpenAddDebtDialog -> HomeAction.OpenAddDebtDialog(
                intent.contactPermission,
                intent.requestCode
            )

            HomeIntention.SyncWithContacts -> HomeAction.SyncWithContacts
        }

    override val reducer: BiFunction<HomeState, HomeResult, HomeState>
        get() = BiFunction { prevState, result ->
            when (result) {
                HomeResult.Error ->
                    prevState.copy(isError = OneShot(true))

                is HomeResult.SortBy -> {
                    prevState.copy(
                        sortType = OneShot(result.sortType)
                    )
                }

                is HomeResult.ShowAddDebtDialog ->
                    prevState.copy(
                        contacts = result.items.map { it.toContactsItemViewModel() },
                        showAddDebtDialog = OneShot(true)
                    )
            }
        }
}
