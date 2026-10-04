package debts.feature.adddebt

import debts.feature.contacts.adapter.ContactsItemViewModel

data class AddDebtUiState(
    val contactId: Long? = null,
    val avatarUrl: String = "",
    val name: String = "",
    val amountText: String = "",
    val amountError: Boolean = false,
    val isSubtract: Boolean = false,
    val comment: String = "",
    val dateMs: Long = System.currentTimeMillis(),
    val showDatePicker: Boolean = false,
    val contacts: List<ContactsItemViewModel> = emptyList(),
    val canChangeDebtor: Boolean = true,
    val existingDebtId: Long? = null,
)
