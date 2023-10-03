package debts.core.usecase

import debts.core.repository.DebtsRepository
import io.reactivex.Completable

class RemoveDebtorUseCase(
    private val repository: DebtsRepository
) {
    fun execute(
        debtorId: Long
    ): Completable = repository.removeDebtor(debtorId)
}
