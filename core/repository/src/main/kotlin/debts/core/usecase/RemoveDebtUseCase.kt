package debts.core.usecase

import debts.core.repository.DebtsRepository

class RemoveDebtUseCase(
    private val repository: DebtsRepository
) {

    suspend fun execute(id: Long) = repository.removeDebt(id)
}
