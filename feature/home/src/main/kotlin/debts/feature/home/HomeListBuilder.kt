package debts.feature.home

import debts.core.repository.SortType
import debts.core.usecase.data.DebtorsListItemModel
import debts.core.usecase.data.TabTypes
import kotlin.math.absoluteValue

internal fun buildHomeTabs(
    debtors: List<DebtorsListItemModel.Debtor>,
    searchQuery: String,
    sortType: SortType,
): Map<TabTypes, HomeTabUiState> =
    TabTypes.entries.associateWith { tabType -> buildHomeTab(debtors, tabType, searchQuery, sortType) }

private fun buildHomeTab(
    debtors: List<DebtorsListItemModel.Debtor>,
    tabType: TabTypes,
    searchQuery: String,
    sortType: SortType,
): HomeTabUiState {
    val query = searchQuery.trim()
    val filtered = debtors
        .filter { debtor ->
            when (tabType) {
                TabTypes.All -> true
                TabTypes.Debtors -> debtor.amount >= 0
                TabTypes.Creditors -> debtor.amount < 0
            }
        }
        .filter { query.isEmpty() || it.name.contains(query, ignoreCase = true) }
    val total = filtered.sumOf { it.amount }
    return HomeTabUiState(
        items = if (tabType == TabTypes.All) {
            section(HomeSection.Debtors, filtered.filter { it.amount >= 0 }, sortType) +
                section(HomeSection.Creditors, filtered.filter { it.amount < 0 }, sortType)
        } else {
            filtered.toSortedItems(sortType)
        },
        total = if (tabType == TabTypes.Creditors) total.absoluteValue else total,
    )
}

private fun section(
    section: HomeSection,
    debtors: List<DebtorsListItemModel.Debtor>,
    sortType: SortType,
): List<HomeListItem> =
    if (debtors.isEmpty()) emptyList() else listOf(HomeListItem.Header(section)) + debtors.toSortedItems(sortType)

private fun List<DebtorsListItemModel.Debtor>.toSortedItems(sortType: SortType): List<HomeListItem.Debtor> {
    val items = map { debtor ->
        HomeListItem.Debtor(
            id = debtor.id,
            name = debtor.name,
            amount = debtor.amount.absoluteValue,
            currency = debtor.currency,
            lastDate = debtor.lastDate,
            avatarUrl = debtor.avatarUrl,
        )
    }
    return when (sortType) {
        SortType.AMOUNT_DESC -> items.sortedByDescending { it.amount }
        SortType.AMOUNT_ASC -> items.sortedBy { it.amount }
        SortType.NAME_DESC -> items.sortedByDescending { it.name }
        SortType.NAME_ASC -> items.sortedBy { it.name }
        SortType.NOTHING -> items
    }
}
