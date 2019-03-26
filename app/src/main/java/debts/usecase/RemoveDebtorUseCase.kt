package debts.usecase

import debts.repository.DebtsRepository
import io.reactivex.Completable

class RemoveDebtorUseCase(
    private val repository: DebtsRepository
) {
    fun execute(
        debtorId: Long
    ): Completable = repository.removeDebtor(debtorId)
}
