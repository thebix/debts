package debts.home.usecase

import debts.home.repository.DebtsRepository
import io.reactivex.Completable
import io.reactivex.Single

class ClearHistoryUseCase(
    private val repository: DebtsRepository
) {

    fun execute(
        debtorId: Long
    ): Completable = repository.clearDebts(debtorId)
}
