package debts.home.usecase

import debts.home.repository.DebtsRepository
import io.reactivex.Completable
import io.reactivex.Single

class AddDebtUseCase(
    private val repository: DebtsRepository,
    private val createDebtorUseCase: CreateDebtorUseCase
) {

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    fun execute(
        debtorId: Long?,
        contactId: Long?,
        name: String,
        amount: Double,
        currency: String,
        comment: String
    ): Completable {
        return when {
            debtorId != null -> Single.fromCallable { debtorId!! }
            else -> {
                repository.getDebtors()
                    .flatMap { items ->
                        contactId?.let { contactId ->
                            items.firstOrNull { it.contactId == contactId }?.let {
                                return@flatMap Single.fromCallable { it.id }
                            }
                        }
                        items.firstOrNull { it.name == name }?.let {
                            return@flatMap Single.fromCallable { it.id }
                        }
                        return@flatMap createDebtorUseCase.execute(name, contactId)
                    }
            }
        }
            .flatMapCompletable { debtor ->
                repository.saveDebt(
                    debtor,
                    amount,
                    currency,
                    comment
                ).ignoreElement()
            }
    }
}
