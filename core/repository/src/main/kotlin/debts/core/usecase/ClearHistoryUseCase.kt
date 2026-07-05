package debts.core.usecase

import debts.core.repository.DebtsRepository

class ClearHistoryUseCase(
    private val repository: DebtsRepository
) {

    suspend fun execute(debtorId: Long) = repository.clearDebts(debtorId)
}
