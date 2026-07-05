package debts.core.usecase

import debts.core.repository.DebtsRepository
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable

class UpdateDebtUseCase(
    private val repository: DebtsRepository
) {

    fun execute(
        id: Long,
        debtorId: Long,
        amount: Double,
        date: Long,
        currency: String,
        comment: String
    ): Completable = rxCompletable {
        repository.updateDebt(
            id = id,
            debtorId = debtorId,
            amount = amount,
            currency = currency,
            date = date,
            comment = comment
        )
    }
}
