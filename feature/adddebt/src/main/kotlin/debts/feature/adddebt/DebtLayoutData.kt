package debts.feature.adddebt

data class DebtLayoutData(
    val contactId: Long?,
    val name: String,
    val amount: Double,
    val comment: String,
    val date: Long,
    val existingDebtId: Long?
)
