package debts.core.usecase

import debts.core.repository.DebtsRepository
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable

class UpdateDbDebtsCurrencyUseCase(
    private val repository: DebtsRepository
) {

    fun execute(): Completable = rxCompletable { repository.updateDebtsCurrency() }
}
