package debts.usecase

import com.gojuno.koptional.None
import com.gojuno.koptional.toOptional
import debts.repository.DebtsRepository
import io.reactivex.Single

class CreateDebtorUseCase(
    private val repository: DebtsRepository
) {

    fun execute(
        name: String,
        contactId: Long?
    ): Single<Long> {
        return if (contactId != null) {
            repository.getContacts()
                .map { contacts ->
                    contacts.firstOrNull { contact ->
                        contact.id == contactId
                    }.toOptional()
                }
        } else {
            Single.fromCallable { None }
        }
            .flatMap { contact ->
                repository.createDebtor(
                    name,
                    contactId,
                    contact.toNullable()?.avatarUrl ?: ""
                )
            }
    }
}
