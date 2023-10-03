package debts.core.usecase.data

import androidx.annotation.StringRes

// region UseCase return models

// TODO: rename to DebtorsItemModel
sealed class DebtorsListItemModel(open val id: Long) {
    data class Debtor(
        override val id: Long,
        val name: String,
        val amount: Double,
        val currency: String,
        val lastDate: Long,
        val avatarUrl: String
    ) : DebtorsListItemModel(id)

    data class Title(@StringRes val titleId: Int) : DebtorsListItemModel(titleId.toLong())
}

data class DebtItemModel(
    val id: Long,
    val amount: Double,
    val currency: String,
    val date: Long,
    val comment: String
)

data class DebtorDetailsModel(
    val name: String,
    val amount: Double,
    val currency: String,
    val avatarUrl: String
)

// endregion
