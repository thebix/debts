package debts.core.usecase

import debts.core.repository.DebtsRepository
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle

class CreateDebtorUseCase(
    private val repository: DebtsRepository,
) {
    fun execute(name: String, contactId: Long?): Single<Long> = rxSingle {
        val avatarUrl = if (contactId != null) {
            repository.getContacts().firstOrNull { it.id == contactId }?.avatarUrl ?: ""
        } else {
            ""
        }
        repository.createDebtor(name, contactId, avatarUrl)
    }
}
