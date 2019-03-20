package debts.home.list.mvi

import debts.common.android.mvi.*
import debts.home.list.adapter.ContactsItemViewModel
import debts.home.list.adapter.DebtorsItemViewModel
import debts.home.usecase.ContactsItemModel
import debts.home.usecase.DebtorsListItemModel

sealed class DebtorsIntention : MviIntention {

    object Init : DebtorsIntention(), MviInitIntention
    object GetContacts : DebtorsIntention()
    data class AddDebt(
        val contactId: Long?,
        val name: String,
        val amount: Double,
        val currency: String,
        val comment: String

    ) : DebtorsIntention()

    data class Filter(val name: String = "") : DebtorsIntention()
    object ToggleSortByName : DebtorsIntention()
    object ToggleSortByAmount : DebtorsIntention()
}

sealed class DebtorsAction : MviAction {

    object Init : DebtorsAction()
    object GetContacts : DebtorsAction()
    data class AddDebt(
        val contactId: Long?,
        val name: String,
        val amount: Double,
        val currency: String,
        val comment: String
    ) : DebtorsAction()

    data class Filter(val name: String = "") : DebtorsAction()
    data class SortBy(
        val sortType: DebtorsState.SortType = DebtorsState.SortType.NOTHING
    ) : DebtorsAction()
}

sealed class DebtorsResult : MviResult {

    data class ItemsResult(
        val items: List<DebtorsListItemModel> = emptyList()
    ) : DebtorsResult()

    object Error : DebtorsResult()
    data class Contacts(
        val items: List<ContactsItemModel> = emptyList()
    ) : DebtorsResult()

    data class Filter(val name: String) : DebtorsResult()

    data class SortBy(
        val sortType: DebtorsState.SortType = DebtorsState.SortType.NOTHING
    ) : DebtorsResult()
}

data class DebtorsState(
    val items: List<DebtorsListItemModel> = emptyList(),
    val filteredItems: List<DebtorsItemViewModel> = emptyList(),
    val isError: OneShot<Boolean> = OneShot.empty(),
    val contacts: OneShot<List<ContactsItemViewModel>> = OneShot.empty(),
    val nameFilter: String = "",
    val sortType: SortType = SortType.NOTHING
) : MviState, ViewStateWithId() {

    enum class SortType {
        NOTHING,
        AMOUNT_DESC,
        AMOUNT_ASC,
        NAME_DESC,
        NAME_ASC
    }
}
