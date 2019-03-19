package debts.home.usecase

import debts.home.repository.DebtsRepository
import io.reactivex.Completable

class RemoveDebtUseCase(
    private val repository: DebtsRepository
) {

    fun execute(
        id: Long
    ): Completable {
        return repository.removeDebt(id)
    }
}
