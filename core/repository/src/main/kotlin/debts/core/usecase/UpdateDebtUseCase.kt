package debts.core.usecase

import debts.core.repository.DebtsRepository
import io.reactivex.Completable

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
    ): Completable = repository.updateDebt(
        id = id,
        debtorId = debtorId,
        amount = amount,
        currency = currency,
        date = date,
        comment = comment
    )
}
