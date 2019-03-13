package debts.home.list.adapter

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

// endregion
