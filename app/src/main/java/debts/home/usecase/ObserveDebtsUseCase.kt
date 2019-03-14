package debts.home.usecase

import debts.home.repository.DebtsRepository
import io.reactivex.Observable

class ObserveDebtsUseCase(
    private val repository: DebtsRepository
) {

    fun execute(debtorId: Long): Observable<List<DebtItemModel>> =
        repository.observeDebts(debtorId)
            .map { items ->
                items.map {
                    DebtItemModel(
                        it.id,
                        it.amount,
                        it.currency,
                        it.date,
                        it.comment
                    )
                }
            }
}
