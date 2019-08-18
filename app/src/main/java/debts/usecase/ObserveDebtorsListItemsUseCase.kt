package debts.usecase

import debts.home.list.TabTypes
import debts.repository.DebtsRepository
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class ObserveDebtorsListItemsUseCase(
    private val repository: DebtsRepository
) {

    fun execute(tabType: TabTypes): Observable<List<DebtorsListItemModel>> {
        return Observable
            .combineLatest(
                repository.observeDebtors(),
                repository.observeDebts(),
                BiFunction { debtors, debts ->
                    return@BiFunction debtors.map { debtor ->
                        val debtorDebts = debts.filter { it.debtorId == debtor.id }
                        val amount = debtorDebts.sumByDouble { it.amount }
                        val lastDebt = debts.sortedByDescending { it.date }
                            .firstOrNull { it.debtorId == debtor.id }
                        DebtorsListItemModel(
                            debtor.id,
                            debtor.name,
                            amount,
                            lastDebt?.currency ?: "",
                            lastDebt?.date ?: 0,
                            debtor.avatarUrl
                        )
                    }
                        .filter { debtor ->
                            tabType == TabTypes.All ||
                                    (tabType == TabTypes.Debtors && debtor.amount >= 0) ||
                                    (tabType == TabTypes.Creditors && debtor.amount < 0)
                        }
                }
            )
    }
}
