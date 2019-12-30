package debts.usecase

import debts.repository.DebtsRepository
import io.reactivex.Completable

class UpdateDbDebtsCurrencyUseCase(
    private val repository: DebtsRepository
) {

    fun execute(): Completable = repository.updateDebtsCurrency()
}
