package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.repository.data.DebtModel
import debts.core.usecase.data.DebtorDetailsModel
import io.reactivex.Observable
import io.reactivex.functions.Function3

class ObserveDebtorUseCase(
    private val repository: DebtsRepository
) {

    fun execute(debtorId: Long): Observable<DebtorDetailsModel> =
        Observable.combineLatest(
            repository.observeDebtor(debtorId),
            repository.observeDebts(debtorId)
                .startWith(emptyList<DebtModel>()),
            repository.observeCurrency(),
            Function3 { debtor, debts, currency ->
                DebtorDetailsModel(
                    debtor.name,
                    debts.sumByDouble { it.amount },
                    currency,
                    debtor.avatarUrl
                )
            }
        )
}
