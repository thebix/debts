package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.usecase.data.DebtorsListItemModel
import debts.core.usecase.data.TabTypes
import io.reactivex.Observable
import io.reactivex.functions.Function3

class ObserveDebtorsListItemsUseCase(
    private val repository: DebtsRepository
) {

    fun execute(tabType: TabTypes): Observable<List<DebtorsListItemModel.Debtor>> {
        return Observable
            .combineLatest(
                repository.observeDebtors(),
                repository.observeDebts(),
                repository.observeCurrency(),
                Function3 { debtors, debts, defaultCurrency ->
                    return@Function3 debtors.map { debtor ->
                        val debtorDebts = debts.filter { it.debtorId == debtor.id }
                        val amount = debtorDebts.sumByDouble { it.amount }
                        val lastDebt = debts.sortedByDescending { it.date }
                            .firstOrNull { it.debtorId == debtor.id }
                        DebtorsListItemModel.Debtor(
                            debtor.id,
                            debtor.name,
                            amount,
                            lastDebt?.currency ?: defaultCurrency,
                            lastDebt?.date ?: Long.MIN_VALUE,
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
