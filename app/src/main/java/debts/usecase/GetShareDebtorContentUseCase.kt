package debts.usecase

import debts.repository.DebtsRepository
import io.reactivex.Single
import io.reactivex.functions.Function3
import timber.log.Timber

class GetShareDebtorContentUseCase(
    private val repository: DebtsRepository
) {

    fun execute(
        debtorId: Long,
        templateBorrowed: String,
        templateLent: String
    ): Single<String> {
        return Single.zip(
            repository.observeDebtor(debtorId = debtorId)
                .take(1)
                .singleOrError(),
            repository.getDebts(debtorId),
            repository.getCurrency(),
            Function3() { debtor, debts, currency ->
                val amount = debts.sumByDouble { it.amount }
                val isBorrowed = amount < 0
                try {
                    return@Function3 String.format(
                        (if (isBorrowed) templateBorrowed else templateLent),
                        debtor.name,
                        amount,
                        currency
                    )
                } catch (ex: Exception) {
                    Timber.e(ex)
                    return@Function3 ""
                }
            }
        )

    }

}
