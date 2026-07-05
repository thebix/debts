package debts.core.usecase

import debts.core.repository.DebtsRepository
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable

class RemoveDebtUseCase(
    private val repository: DebtsRepository
) {

    fun execute(id: Long): Completable = rxCompletable { repository.removeDebt(id) }
}
