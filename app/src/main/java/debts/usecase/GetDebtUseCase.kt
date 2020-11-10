package debts.usecase

import debts.repository.DebtsRepository
import io.reactivex.Single

class GetDebtUseCase(
    private val repository: DebtsRepository
) {

    fun execute(
        id: Long
    ): Single<DebtModel> {
        return repository.getDebt(id)
    }
}
