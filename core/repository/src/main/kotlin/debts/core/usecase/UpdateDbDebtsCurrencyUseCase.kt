package debts.core.usecase

import debts.core.repository.DebtsRepository

class UpdateDbDebtsCurrencyUseCase(
    private val repository: DebtsRepository
) {

    suspend fun execute() = repository.updateDebtsCurrency()
}
