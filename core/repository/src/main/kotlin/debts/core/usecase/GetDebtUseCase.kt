package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.repository.data.DebtModel

class GetDebtUseCase(
    private val repository: DebtsRepository
) {

    suspend fun execute(id: Long): DebtModel = repository.getDebt(id)
}
