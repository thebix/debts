package debts.core.usecase

import debts.core.common.android.extensions.toSimpleDateTimeString
import debts.core.repository.DebtsRepository
import java.util.Date

class GetDebtsCsvContentUseCase(
    private val repository: DebtsRepository,
) {

    suspend fun execute(): String {
        val debtors = repository.getDebtors()
        val debts = repository.getDebts()
        val currency = repository.getCurrency()
        val debtorsMap = debtors.associate { it.id to it }
        // Tech debt: pass translated strings from Fragment
        val sb = StringBuilder("Date,\tName,\tAmount,\tCurrency,\tComment")
        debts.forEach { debt ->
            debtorsMap[debt.debtorId]?.let { debtor ->
                sb.append("\n${Date(debt.date).toSimpleDateTimeString()},\t${debtor.name},\t${debt.amount},\t$currency,\t${debt.comment}")
            }
        }
        return sb.toString()
    }
}
