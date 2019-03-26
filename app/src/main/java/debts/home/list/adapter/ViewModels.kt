package debts.home.list.adapter

import debts.usecase.ContactsItemModel
import debts.usecase.DebtorsListItemModel

sealed class DebtorsItemViewModel(open val id: Long) {

    data class DebtorItemViewModel(
        override val id: Long,
        val name: String,
        val amount: Double,
        val currency: String,
        val lastDate: Long,
        val avatarUrl: String
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
        lastDate,
        avatarUrl
    )

fun ContactsItemModel.toContactsItemViewModel() =
    ContactsItemViewModel(
        id, name, avatarUrl
    )

// endregion
