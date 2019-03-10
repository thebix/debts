package debts.home.usecase

// region Repo return models
////////////////////////////////////////////////////////////////

data class DebtorModel(
    val id: Long,
    val name: String
)

data class DebtModel(
    val id: Long,
    val debtorId: Long,
    val amount: Double,
    val currency: String,
    val date: Long
)

// endregion

// region UseCase return models
////////////////////////////////////////////////////////////////

data class DebtorsListItemModel(
    val id: Long,
    val name: String,
    val amount: Double,
    val currency: String,
    val lastDate: Long
)

// endregion
