package debts.usecase

// region Repo return models
////////////////////////////////////////////////////////////////

data class DebtorModel(
    val id: Long,
    val name: String,
    val contactId: Long?,
    val avatarUrl: String
)

data class DebtModel(
    val id: Long,
    val debtorId: Long,
    val amount: Double,
    val currency: String,
    val date: Long,
    val comment: String
)

data class ContactsItemModel(val id: Long, val name: String, val avatarUrl: String)

// endregion

// region UseCase return models
////////////////////////////////////////////////////////////////

// TODO: rename to DebtorsItemModel
data class DebtorsListItemModel(
    val id: Long,
    val name: String,
    val amount: Double,
    val currency: String,
    val lastDate: Long,
    val avatarUrl: String
)

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
