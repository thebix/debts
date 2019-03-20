package debts.home.usecase

import debts.home.repository.DebtsRepository
import io.reactivex.Completable

class RemoveDebtorUseCase(
    private val repository: DebtsRepository
) {
    fun execute(
        debtorId: Long
    ): Completable = repository.removeDebtor(debtorId)
}
