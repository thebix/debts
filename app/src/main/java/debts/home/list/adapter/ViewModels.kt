package debts.home.list.adapter

import androidx.annotation.StringRes
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

    // INFO: title must be unique for adapter
    data class TitleItem(@StringRes val titleId: Int) : DebtorsItemViewModel(titleId.toLong())
}

data class ContactsItemViewModel(val id: Long, val name: String, val avatarUrl: String)

// region Mapping

fun DebtorsListItemModel.toDebtorsItemViewModel() = when (this) {
    is DebtorsListItemModel.Debtor -> DebtorsItemViewModel.DebtorItemViewModel(
        id,
        name,
        amount,
        currency,
        lastDate,
        avatarUrl
    )
    is DebtorsListItemModel.Title -> DebtorsItemViewModel.TitleItem(id.toInt())
}

fun ContactsItemModel.toContactsItemViewModel() =
    ContactsItemViewModel(
        id, name, avatarUrl
    )

// endregion
