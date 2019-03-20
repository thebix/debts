package debts.home.list.mvi

import debts.common.android.mvi.MviViewModel
import debts.common.android.mvi.OneShot
import debts.home.list.adapter.toContactsItemViewModel
import debts.home.list.adapter.toDebtorsItemViewModel
import debts.home.usecase.DebtorsListItemModel
import io.reactivex.functions.BiFunction

class DebtorsViewModel(
    interactor: DebtorsInteractor
) : MviViewModel<DebtorsIntention, DebtorsAction, DebtorsResult, DebtorsState>(interactor) {

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
        }

    override val reducer: BiFunction<DebtorsState, DebtorsResult, DebtorsState>
        get() = BiFunction { prevState, result ->
            when (result) {
                is DebtorsResult.ItemsResult -> {

                    prevState.copy(
                        items = result.items,
                        filteredItems = filterAndMapDebtors(
                            result.items,
                            prevState.nameFilter
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
                        filteredItems = filterAndMapDebtors(
                            prevState.items,
                            result.name
                        )
                    )
            }
        }

    private fun filterAndMapDebtors(items: List<DebtorsListItemModel>, name: String) =
        if (name.isNotBlank())
            items.filter {
                it.name.contains(
                    name,
                    true
                )
            }.map { it.toDebtorsItemViewModel() }
        else
            items.map { it.toDebtorsItemViewModel() }


}
