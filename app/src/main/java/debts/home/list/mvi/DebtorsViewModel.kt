package debts.home.list.mvi

import debts.common.android.mvi.MviViewModel
import debts.common.android.mvi.OneShot
import debts.home.list.adapter.toContactsItemViewModel
import debts.home.list.adapter.toDebtorsItemViewModel
import debts.repository.SortType
import io.reactivex.functions.BiFunction

class DebtorsViewModel(
    interactor: DebtorsInteractor
) : MviViewModel<DebtorsIntention, DebtorsAction, DebtorsResult, DebtorsState>(interactor) {

    private var sortType: SortType = SortType.NOTHING

    override val defaultState: DebtorsState
        get() = DebtorsState()

    override fun actionFromIntention(intent: DebtorsIntention): DebtorsAction =
        when (intent) {
            is DebtorsIntention.Init -> DebtorsAction.Init(intent.contactPermission, intent.requestCode, intent.tabType)
            is DebtorsIntention.OpenAddDebtDialog -> DebtorsAction.OpenAddDebtDialog(
                intent.contactPermission,
                intent.requestCode
            )
            is DebtorsIntention.AddDebt -> DebtorsAction.AddDebt(
                intent.contactId,
                intent.name,
                intent.amount,
                intent.comment
            )
            DebtorsIntention.ToggleSortByAmount -> DebtorsAction.SortBy(
                when (sortType) {
                    SortType.AMOUNT_ASC -> SortType.AMOUNT_DESC
                    SortType.AMOUNT_DESC -> SortType.NOTHING
                    else -> SortType.AMOUNT_ASC
                }
            )
            DebtorsIntention.ToggleSortByName -> DebtorsAction.SortBy(
                when (sortType) {
                    SortType.NAME_ASC -> SortType.NAME_DESC
                    SortType.NAME_DESC -> SortType.NOTHING
                    else -> SortType.NAME_ASC
                }
            )
            is DebtorsIntention.RemoveDebtor -> DebtorsAction.RemoveDebtor(intent.debtorId)
            is DebtorsIntention.ShareDebtor -> DebtorsAction.ShareDebtor(
                intent.debtorId,
                intent.titleText,
                intent.borrowedTemplate,
                intent.lentTemplate
            )
            is DebtorsIntention.OpenDetails -> DebtorsAction.OpenDetails(
                intent.debtorId,
                intent.rootId
            )
            DebtorsIntention.SyncWithContacts -> DebtorsAction.SyncWithContacts
            DebtorsIntention.OpenSettings -> DebtorsAction.OpenSettings
            is DebtorsIntention.ShareAllDebts -> DebtorsAction.ShareAllDebts(intent.titleText)
        }

    override val reducer: BiFunction<DebtorsState, DebtorsResult, DebtorsState>
        get() = BiFunction { prevState, result ->
            when (result) {
                is DebtorsResult.ItemsResult -> {
                    prevState.copy(
                        items = result.items.map { it.toDebtorsItemViewModel() }
                    )
                }
                DebtorsResult.Error ->
                    prevState.copy(isError = OneShot(true))
                is DebtorsResult.ShowAddDebtDialog ->
                    prevState.copy(
                        contacts = result.items.map { it.toContactsItemViewModel() },
                        showAddDebtDialog = OneShot(true)
                    )
                is DebtorsResult.SortBy -> {
                    sortType = result.sortType
                    prevState.copy(
                        sortType = result.sortType
                    )
                }
            }
        }
}
