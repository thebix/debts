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
}

sealed class DebtorsResult : MviResult {

    data class ItemsResult(
        val items: List<DebtorsListItemModel> = emptyList()
    ) : DebtorsResult()

    object Error : DebtorsResult()
    data class Contacts(
        val items: List<ContactsItemModel> = emptyList()
    ) : DebtorsResult()
}

data class DebtorsState(
    val items: List<DebtorsItemViewModel.DebtorItemViewModel> = emptyList(),
    val isError: OneShot<Boolean> = OneShot.empty(),
    val contacts: OneShot<List<ContactsItemViewModel>> = OneShot.empty()
) : MviState, ViewStateWithId()
