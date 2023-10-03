package debts.core.usecase

import debts.core.repository.DebtsRepository
import io.reactivex.Completable

class ClearHistoryUseCase(
    private val repository: DebtsRepository
) {

    fun execute(
        debtorId: Long
    ): Completable = repository.clearDebts(debtorId)
}
