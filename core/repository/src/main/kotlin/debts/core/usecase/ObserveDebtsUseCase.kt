package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.usecase.data.DebtItemModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveDebtsUseCase(
    private val repository: DebtsRepository
) {

    fun execute(debtorId: Long): Flow<List<DebtItemModel>> =
        repository.observeDebts(debtorId)
            .map { items ->
                items.map { DebtItemModel(it.id, it.amount, it.currency, it.date, it.comment) }
            }
}
