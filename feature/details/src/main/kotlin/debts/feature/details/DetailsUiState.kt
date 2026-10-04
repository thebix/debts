package debts.feature.details

import debts.core.usecase.data.DebtItemModel

data class DetailsUiState(
    val name: String = "",
    val avatarUrl: String = "",
    /** Absolute value: the screen does not show who owes whom in the total. */
    val amount: Double = 0.0,
    val currency: String = "",
    /** Newest first. */
    val debts: List<DebtItemModel> = emptyList(),
)
