package debts.home.list.mvi

import debts.common.android.mvi.MviViewModel
import debts.common.android.mvi.OneShot
import debts.home.list.adapter.DebtorsItemViewModel
import debts.home.list.adapter.toContactsItemViewModel
import debts.home.list.adapter.toDebtorsItemViewModel
import debts.home.usecase.DebtorsListItemModel
import io.reactivex.functions.BiFunction

class DebtorsViewModel(
    interactor: DebtorsInteractor
) : MviViewModel<DebtorsIntention, DebtorsAction, DebtorsResult, DebtorsState>(interactor) {

    private var sortType: DebtorsState.SortType = DebtorsState.SortType.NOTHING

    override val defaultState: DebtorsState
        get() = DebtorsState()

    override fun actionFromIntention(intent: DebtorsIntention): DebtorsAction =
        when (intent) {
            DebtorsIntention.Init -> DebtorsAction.Init
            DebtorsIntention.GetContacts -> DebtorsAction.GetContacts
            is DebtorsIntention.AddDebt -> DebtorsAction.AddDebt(
                intent.contactId,
                intent.name,
                intent.amount,
                intent.currency,
                intent.comment
            )
            is DebtorsIntention.Filter -> DebtorsAction.Filter(intent.name)
            DebtorsIntention.ToggleSortByAmount -> DebtorsAction.SortBy(
                when (sortType) {
                    DebtorsState.SortType.AMOUNT_ASC -> DebtorsState.SortType.AMOUNT_DESC
                    DebtorsState.SortType.AMOUNT_DESC -> DebtorsState.SortType.NOTHING
                    else -> DebtorsState.SortType.AMOUNT_ASC
                }
            )
            DebtorsIntention.ToggleSortByName -> DebtorsAction.SortBy(
                when (sortType) {
                    DebtorsState.SortType.NAME_ASC -> DebtorsState.SortType.NAME_DESC
                    DebtorsState.SortType.NAME_DESC -> DebtorsState.SortType.NOTHING
                    else -> DebtorsState.SortType.NAME_ASC
                }
            )
            is DebtorsIntention.RemoveDebtor -> DebtorsAction.RemoveDebtor(intent.debtorId)
        }

    override val reducer: BiFunction<DebtorsState, DebtorsResult, DebtorsState>
        get() = BiFunction { prevState, result ->
            when (result) {
                is DebtorsResult.ItemsResult -> {
                    prevState.copy(
                        items = result.items,
                        filteredItems = filterSortAndMapDebtors(
                            result.items,
                            prevState.nameFilter,
                            prevState.sortType
                        )
                    )
                }
                DebtorsResult.Error ->
                    prevState.copy(isError = OneShot(true))
                is DebtorsResult.Contacts ->
                    prevState.copy(contacts = OneShot(result.items.map { it.toContactsItemViewModel() }))
                is DebtorsResult.Filter ->
                    prevState.copy(
                        nameFilter = result.name,
                        filteredItems = filterSortAndMapDebtors(
                            prevState.items,
                            result.name,
                            prevState.sortType
                        )
                    )
                is DebtorsResult.SortBy -> {
                    sortType = result.sortType
                    prevState.copy(
                        sortType = result.sortType,
                        filteredItems = filterSortAndMapDebtors(
                            prevState.items,
                            prevState.nameFilter,
                            result.sortType
                        )
                    )
                }
            }
        }

    private fun filterSortAndMapDebtors(
        items: List<DebtorsListItemModel>,
        name: String,
        sortType: DebtorsState.SortType
    ): List<DebtorsItemViewModel> {
        val filtered = if (name.isNotBlank())
            items.filter {
                it.name.contains(
                    name,
                    true
                )
            }
        else
            items

        return when (sortType) {
            DebtorsState.SortType.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
            DebtorsState.SortType.AMOUNT_ASC -> filtered.sortedBy { it.amount }
            DebtorsState.SortType.NAME_DESC -> filtered.sortedByDescending { it.name }
            DebtorsState.SortType.NAME_ASC -> filtered.sortedBy { it.name }
            else -> filtered
        }.map { it.toDebtorsItemViewModel() }
    }

}
