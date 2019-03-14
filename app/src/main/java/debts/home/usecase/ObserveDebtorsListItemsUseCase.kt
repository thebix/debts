package debts.home.usecase

import debts.home.repository.DebtsRepository
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class ObserveDebtorsListItemsUseCase(
    private val repository: DebtsRepository
) {

    fun execute(): Observable<List<DebtorsListItemModel>> {
        return Observable
            .combineLatest(
                repository.observeDebtors(),
                repository.observeDebts(),
                BiFunction { debtors, debts ->
                    return@BiFunction debtors.map { debtor ->
                        val debtorDebts = debts.filter { it.debtorId == debtor.id }
                        val amount = debtorDebts.sumByDouble { it.amount }
                        val lastDebt = debts.sortedByDescending { it.date }
                            .lastOrNull { it.debtorId == debtor.id }
                        DebtorsListItemModel(
                            debtor.id,
                            debtor.name,
                            amount,
                            lastDebt?.currency ?: "",
                            lastDebt?.date ?: 0,
                            debtor.avatarUrl
                        )
                    }
                }
            )
    }
}
