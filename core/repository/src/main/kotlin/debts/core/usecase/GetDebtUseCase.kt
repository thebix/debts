package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.repository.data.DebtModel
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle

class GetDebtUseCase(
    private val repository: DebtsRepository
) {

    fun execute(id: Long): Single<DebtModel> = rxSingle { repository.getDebt(id) }
}
