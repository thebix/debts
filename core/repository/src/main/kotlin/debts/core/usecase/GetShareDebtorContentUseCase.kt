package debts.core.usecase

import debts.core.common.android.extensions.toFormattedCurrency
import debts.core.repository.DebtsRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import timber.log.Timber
import kotlin.math.absoluteValue

class GetShareDebtorContentUseCase(
    private val repository: DebtsRepository,
) {

    suspend fun execute(
        debtorId: Long,
        templateBorrowed: String,
        templateLent: String,
    ): String {
        val debtor = repository.observeDebtor(debtorId).filterNotNull().first()
        val debts = repository.getDebts(debtorId)
        val currency = repository.getCurrency()
        val amount = debts.sumOf { it.amount }
        val isBorrowed = amount < 0
        return runCatching {
            String.format(
                if (isBorrowed) templateBorrowed else templateLent,
                debtor.name,
                amount.absoluteValue.toFormattedCurrency(),
                currency
            )
        }.onFailure {
            Timber.e(it)
        }.getOrDefault("")
    }
}
