package debts.feature.home

import debts.feature.contacts.adapter.ContactsItemViewModel

sealed interface HomeEvent {

    data class OpenDetails(val debtorId: Long) : HomeEvent
    data object OpenSettings : HomeEvent
    data class ShareDebtor(val message: String) : HomeEvent
    data class ShareAllDebts(val csvContent: String) : HomeEvent
    data class RequestContactsPermission(val purpose: ContactsPermissionPurpose) : HomeEvent
    data class ShowAddDebtDialog(val contacts: List<ContactsItemViewModel>) : HomeEvent
    data object DebtAdded : HomeEvent
    data object EmptyDebtFields : HomeEvent
}

enum class ContactsPermissionPurpose {
    AddDebt,
    Sync,
}
