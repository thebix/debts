package debts.core.usecase

import debts.core.repository.DebtsRepository

class UpdateDebtUseCase(
    private val repository: DebtsRepository
) {

    suspend fun execute(
        id: Long,
        debtorId: Long,
        amount: Double,
        date: Long,
        currency: String,
        comment: String
    ) = repository.updateDebt(
        id = id,
        debtorId = debtorId,
        amount = amount,
        currency = currency,
        date = date,
        comment = comment
    )
}
