package debts.details.adapter

import debts.core.usecase.data.DebtItemModel

sealed class DebtsItemViewModel(open val id: Long) {

    data class DebtItemViewModel(
        override val id: Long,
        val amount: Double,
        val currency: String,
        val date: Long,
        val comment: String
    ) : DebtsItemViewModel(id)
}

// region Mapping

fun DebtItemModel.toDebtsItemViewModel() =
    DebtsItemViewModel.DebtItemViewModel(
        id,
        amount,
        currency,
        date,
        comment
    )

// endregion
