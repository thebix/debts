package debts.core.usecase

import debts.core.repository.DebtsRepository

class RemoveDebtorUseCase(
    private val repository: DebtsRepository
) {
    suspend fun execute(debtorId: Long) = repository.removeDebtor(debtorId)
}
