package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.usecase.data.DebtorDetailsModel
import io.reactivex.Observable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.rx2.asObservable

class ObserveDebtorUseCase(
    private val repository: DebtsRepository
) {

    fun execute(debtorId: Long): Observable<DebtorDetailsModel> =
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
        }.asObservable()
}
