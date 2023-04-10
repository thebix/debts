package debts.usecase

import debts.repository.DebtsRepository
import io.reactivex.Single

class CreateDebtorUseCase(
    private val repository: DebtsRepository,
) {
    fun execute(
        name: String,
        contactId: Long?,
    ): Single<Long> {
        return if (contactId != null) {
            repository.getContacts()
                .map { contacts ->
                    val contact = contacts.firstOrNull { contact ->
                        contact.id == contactId
                    }
                    AvatarUrl(contact?.avatarUrl ?: "")
                }
        } else {
            Single.fromCallable { AvatarUrl("") }
        }
            .flatMap { avatarUrl ->
                repository.createDebtor(
                    name,
                    contactId,
                    avatarUrl.url
                )
            }
    }

    @JvmInline
    private value class AvatarUrl(val url: String)
}
