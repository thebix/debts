package debts.feature.home

import debts.core.repository.SortType
import debts.core.usecase.data.TabTypes

data class HomeUiState(
    val tabs: Map<TabTypes, HomeTabUiState> = TabTypes.entries.associateWith { HomeTabUiState() },
    val currency: String = "",
    val sortType: SortType = SortType.NOTHING,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
)

data class HomeTabUiState(
    val items: List<HomeListItem> = emptyList(),
    val total: Double = 0.0,
)

sealed interface HomeListItem {

    data class Header(val section: HomeSection) : HomeListItem

    data class Debtor(
        val id: Long,
        val name: String,
        val amount: Double,
        val currency: String,
        val lastDate: Long,
        val avatarUrl: String,
    ) : HomeListItem
}

enum class HomeSection {
    Debtors,
    Creditors,
}
