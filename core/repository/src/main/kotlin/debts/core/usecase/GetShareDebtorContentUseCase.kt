package debts.core.usecase

import debts.core.common.android.extensions.toFormattedCurrency
import debts.core.repository.DebtsRepository
import io.reactivex.Single
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.rxSingle
import timber.log.Timber
import kotlin.math.absoluteValue

class GetShareDebtorContentUseCase(
    private val repository: DebtsRepository,
) {

    fun execute(
        debtorId: Long,
        templateBorrowed: String,
        templateLent: String,
    ): Single<String> = rxSingle {
        val debtor = repository.observeDebtor(debtorId).filterNotNull().first()
        val debts = repository.getDebts(debtorId)
        val currency = repository.getCurrency()
        val amount = debts.sumOf { it.amount }
        val isBorrowed = amount < 0
        runCatching {
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
