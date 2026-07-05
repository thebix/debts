package debts.core.usecase

import debts.core.repository.DebtsRepository
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable

class RemoveDebtorUseCase(
    private val repository: DebtsRepository
) {
    fun execute(debtorId: Long): Completable = rxCompletable { repository.removeDebtor(debtorId) }
}
