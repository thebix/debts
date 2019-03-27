package debts.usecase

import debts.common.android.extensions.toSimpleDateTimeString
import debts.repository.DebtsRepository
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.*

class GetDebtsCsvContentUseCase(
    private val repository: DebtsRepository
) {

    fun execute(): Single<String> {
        return Single.zip(
            repository.getDebtors(),
            repository.getDebts(),
            BiFunction { debtors, debts ->
                val debtorsMap = debtors.map { it.id to it }.toMap()
                // Tech debt: pass translated strings from Fragment
                val sb = StringBuilder("Date,\tName,\tAmount,\tComment")
                debts.forEach { debt ->
                    debtorsMap[debt.debtorId]?.let { debtor ->
                        sb.append("\n${Date(debt.date).toSimpleDateTimeString()},\t${debtor.name},\t${debt.amount},\t${debt.comment}")
                    }
                }
                sb.toString()
            }
        )

    }

}
