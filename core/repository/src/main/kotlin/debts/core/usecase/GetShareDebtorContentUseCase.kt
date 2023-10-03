package debts.core.usecase

import debts.core.common.android.extensions.toFormattedCurrency
import debts.core.repository.DebtsRepository
import io.reactivex.Single
import io.reactivex.functions.Function3
import timber.log.Timber
import kotlin.math.absoluteValue

class GetShareDebtorContentUseCase(
    private val repository: DebtsRepository,
) {

    fun execute(
        debtorId: Long,
        templateBorrowed: String,
        templateLent: String,
    ): Single<String> {
        return Single.zip(
            repository.observeDebtor(debtorId = debtorId)
                .take(1)
                .singleOrError(),
            repository.getDebts(debtorId),
            repository.getCurrency(),
            Function3 { debtor, debts, currency ->
                val amount = debts.sumByDouble { it.amount }
                val isBorrowed = amount < 0
                runCatching {
                    String.format(
                        (if (isBorrowed) templateBorrowed else templateLent),
                        debtor.name,
                        amount.absoluteValue.toFormattedCurrency(),
                        currency
                    )
                }.onFailure {
                    Timber.e(it)
                }.getOrDefault("")
            }
        )
    }
}
