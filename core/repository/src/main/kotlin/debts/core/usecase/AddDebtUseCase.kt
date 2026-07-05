package debts.core.usecase

import debts.core.repository.DebtsRepository

class AddDebtUseCase(
    private val repository: DebtsRepository,
    private val createDebtorUseCase: CreateDebtorUseCase
) {

    suspend fun execute(
        debtorId: Long?,
        contactId: Long?,
        name: String,
        amount: Double,
        currency: String,
        comment: String,
        date: Long
    ) {
        val finalDebtorId: Long = if (debtorId != null) {
            debtorId
        } else {
            val debtors = repository.getDebtors()
            contactId?.let { cId -> debtors.firstOrNull { it.contactId == cId }?.id }
                ?: debtors.firstOrNull { it.name == name }?.id
                ?: createDebtorUseCase.execute(name, contactId)
        }
        repository.saveDebt(finalDebtorId, amount, currency, comment, date)
    }
}
