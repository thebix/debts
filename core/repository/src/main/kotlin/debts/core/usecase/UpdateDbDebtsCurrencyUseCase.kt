package debts.core.usecase

import debts.core.repository.DebtsRepository
import io.reactivex.Completable

class UpdateDbDebtsCurrencyUseCase(
    private val repository: DebtsRepository
) {

    fun execute(): Completable = repository.updateDebtsCurrency()
}
