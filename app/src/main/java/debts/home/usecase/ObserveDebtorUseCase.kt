package debts.home.usecase

import debts.home.repository.DebtsRepository
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class ObserveDebtorUseCase(
    private val repository: DebtsRepository
) {

    fun execute(debtorId: Long): Observable<DebtorDetailsModel> =
        Observable.combineLatest(
            repository.observeDebtor(debtorId),
            repository.observeDebts(debtorId)
                .startWith(emptyList<DebtModel>()),
            BiFunction { debtor, debts ->
                DebtorDetailsModel(
                    debtor.name,
                    debts.sumByDouble { it.amount },
                    // TODO: use proper currency
                    "$",
                    debtor.avatarUrl
                )
            }
        )
}
