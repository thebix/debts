package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.usecase.data.DebtItemModel
import io.reactivex.Observable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asObservable

class ObserveDebtsUseCase(
    private val repository: DebtsRepository
) {

    fun execute(debtorId: Long): Observable<List<DebtItemModel>> =
        repository.observeDebts(debtorId)
            .map { items ->
                items.map { DebtItemModel(it.id, it.amount, it.currency, it.date, it.comment) }
            }
            .asObservable()
}
