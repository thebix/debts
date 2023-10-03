package debts.core.repository.data

// region Repo return models

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
