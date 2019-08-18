package debts.home.list.mvi

import debts.common.android.mvi.MviViewModel
import debts.common.android.mvi.OneShot
import debts.home.list.TabTypes
import debts.home.list.adapter.DebtorsItemViewModel
import debts.home.list.adapter.toContactsItemViewModel
import debts.home.list.adapter.toDebtorsItemViewModel
import debts.usecase.DebtorsListItemModel
import io.reactivex.functions.BiFunction
import net.thebix.debts.R
import kotlin.math.absoluteValue

class DebtorsViewModel(
    interactor: DebtorsInteractor
) : MviViewModel<DebtorsIntention, DebtorsAction, DebtorsResult, DebtorsState>(interactor) {

    private var sortType: DebtorsState.SortType = DebtorsState.SortType.NOTHING

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
                        items = result.items,
                        filteredItems = getDebtorsWithAllFiltersApplied(
                            result.items,
                            prevState.nameFilter,
                            prevState.sortType,
                            result.tabType == TabTypes.All
                        )
                    )
                }
                DebtorsResult.Error ->
                    prevState.copy(isError = OneShot(true))
                is DebtorsResult.ShowAddDebtDialog ->
                    prevState.copy(
                        contacts = result.items.map { it.toContactsItemViewModel() },
                        showAddDebtDialog = OneShot(true)
                    )
                is DebtorsResult.Filter ->
                    prevState.copy(
                        nameFilter = result.name,
                        filteredItems = getDebtorsWithAllFiltersApplied(
                            prevState.items,
                            result.name,
                            prevState.sortType
                        )
                    )
                is DebtorsResult.SortBy -> {
                    sortType = result.sortType
                    prevState.copy(
                        sortType = result.sortType,
                        filteredItems = getDebtorsWithAllFiltersApplied(
                            prevState.items,
                            prevState.nameFilter,
                            result.sortType
                        )
                    )
                }
            }
        }

    private fun getDebtorsWithAllFiltersApplied(
        items: List<DebtorsListItemModel>,
        name: String,
        sortType: DebtorsState.SortType,
        showTitles: Boolean = false
    ): List<DebtorsItemViewModel> {
        val filtered = getFiltered(items, name)
        @Suppress("UnnecessaryVariable")
        val filteredAndWithAbsAmountsAndSortedAndTitled: List<DebtorsItemViewModel> = if (showTitles) {
            val debtors = filtered.filter { it.amount >= 0 }
            val creditors = filtered.filter { it.amount < 0 }
            return if (debtors.isNotEmpty()) {
                listOf(DebtorsItemViewModel.TitleItem(R.string.home_pager_tab_debtors))
            } else {
                emptyList()
            }
                .plus(
                    getWithAbsAmountsAndSorted(debtors, sortType).map { item -> item.toDebtorsItemViewModel() }
                )
                .plus(
                    if (creditors.isNotEmpty()) {
                        listOf(DebtorsItemViewModel.TitleItem(R.string.home_pager_tab_creditors))
                    } else {
                        emptyList()
                    }
                )
                .plus(
                    getWithAbsAmountsAndSorted(creditors, sortType).map { item -> item.toDebtorsItemViewModel() }
                )
        } else {
            getWithAbsAmountsAndSorted(items, sortType).map { item -> item.toDebtorsItemViewModel() }
        }

        return filteredAndWithAbsAmountsAndSortedAndTitled
    }

    private fun getFiltered(
        items: List<DebtorsListItemModel>,
        name: String
    ): List<DebtorsListItemModel> =
        if (name.isNotBlank())
            items.filter {
                it.name.contains(
                    name,
                    true
                )
            }
        else
            items

    private fun getWithAbsAmountsAndSorted(
        items: List<DebtorsListItemModel>,
        sortType: DebtorsState.SortType
    ): List<DebtorsListItemModel> {
        val absoluteAmounts =
            items.map { item ->
                item.copy(
                    amount = item.amount.absoluteValue
                )
            }

        return when (sortType) {
            DebtorsState.SortType.AMOUNT_DESC -> absoluteAmounts.sortedByDescending { it.amount }
            DebtorsState.SortType.AMOUNT_ASC -> absoluteAmounts.sortedBy { it.amount }
            DebtorsState.SortType.NAME_DESC -> absoluteAmounts.sortedByDescending { it.name }
            DebtorsState.SortType.NAME_ASC -> absoluteAmounts.sortedBy { it.name }
            else -> absoluteAmounts
        }
    }
}
