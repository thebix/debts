package debts.home.list.mvi

import debts.common.android.mvi.MviViewModel
import debts.common.android.mvi.OneShot
import debts.repository.SortType
import io.reactivex.functions.BiFunction

class HomeViewModel(
    interactor: HomeInteractor
) : MviViewModel<HomeIntention, HomeAction, HomeResult, HomeState>(interactor) {

    private var sortType: SortType = SortType.NOTHING

    override val defaultState: HomeState
        get() = HomeState()

    override fun actionFromIntention(intent: HomeIntention): HomeAction =
        when (intent) {
            HomeIntention.Init -> HomeAction.Init
            HomeIntention.InitMenu -> HomeAction.InitMenu
            is HomeIntention.Filter -> HomeAction.Filter(intent.name)
            HomeIntention.ToggleSortByAmount -> HomeAction.SortBy(
                when (sortType) {
                    SortType.AMOUNT_ASC -> SortType.AMOUNT_DESC
                    SortType.AMOUNT_DESC -> SortType.NOTHING
                    else -> SortType.AMOUNT_ASC
                }
            )
            HomeIntention.ToggleSortByName -> HomeAction.SortBy(
                when (sortType) {
                    SortType.NAME_ASC -> SortType.NAME_DESC
                    SortType.NAME_DESC -> SortType.NOTHING
                    else -> SortType.NAME_ASC
                }
            )
            HomeIntention.OpenSettings -> HomeAction.OpenSettings
            is HomeIntention.ShareAllDebts -> HomeAction.ShareAllDebts(intent.titleText)
//            is HomeIntention.AddDebt -> HomeAction.AddDebt(
//                intent.contactId,
//                intent.name,
//                intent.amount,
//                intent.comment
//            )
        }

    override val reducer: BiFunction<HomeState, HomeResult, HomeState>
        get() = BiFunction { prevState, result ->
            when (result) {
                HomeResult.Error ->
                    prevState.copy(isError = OneShot(true))
                is HomeResult.SortBy -> {
                    sortType = result.sortType
                    prevState.copy(
                        sortType = result.sortType
                    )
                }
//                is HomeResult.ShowAddDebtDialog ->
//                    prevState.copy(
//                        contacts = result.items.map { it.toContactsItemViewModel() },
//                        showAddDebtDialog = OneShot(true)
//                    )
            }
        }
}
