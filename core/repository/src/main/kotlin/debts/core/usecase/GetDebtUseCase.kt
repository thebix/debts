package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.repository.data.DebtModel
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
