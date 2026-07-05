package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.usecase.data.DebtorDetailsModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart

class ObserveDebtorUseCase(
    private val repository: DebtsRepository
) {

    fun execute(debtorId: Long): Flow<DebtorDetailsModel> =
        combine(
            repository.observeDebtor(debtorId).filterNotNull(),
            repository.observeDebts(debtorId).onStart { emit(emptyList()) },
            repository.observeCurrency()
        ) { debtor, debts, currency ->
            DebtorDetailsModel(
                debtor.name,
                debts.sumOf { it.amount },
                currency,
                debtor.avatarUrl
            )
        }
}
