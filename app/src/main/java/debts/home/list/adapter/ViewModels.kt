package debts.home.list.adapter

import debts.home.usecase.ContactsItemModel
import debts.home.usecase.DebtorsListItemModel

sealed class DebtorsItemViewModel(open val id: Long) {

    data class DebtorItemViewModel(
        override val id: Long,
        val name: String,
        val amount: Double,
        val currency: String,
        val lastDate: Long
    ) : DebtorsItemViewModel(id)

}

data class ContactsItemViewModel(val id: Long, val name: String, val avatarUrl: String)

// region Mapping
////////////////////////////////////////////////////////////////

fun DebtorsListItemModel.toDebtorsItemViewModel() =
    DebtorsItemViewModel.DebtorItemViewModel(
        id,
        name,
        amount,
        currency,
        lastDate
    )

fun ContactsItemModel.toContactsItemViewModel() =
    ContactsItemViewModel(
        id, name, avatarUrl
    )
// endregion
