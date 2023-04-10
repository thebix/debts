package debts.usecase

import debts.repository.DebtsRepository
import io.reactivex.Completable
import io.reactivex.Single

class SyncDebtorsWithContactsUseCase(
    private val repository: DebtsRepository
) {

    /**
     * forceSync ignores preferences check
     */
    fun execute(forceSync: Boolean = false): Completable =
        if (forceSync) {
            Single.fromCallable { true }
        } else {
            repository.isContactsSynced()
                .map { it.not() }
        }
            .flatMap { isShouldSync ->
                if (isShouldSync) repository.getDebtors() else Single.fromCallable { listOf<DebtorModel>() }
            }
            .map { items ->
                items.filter { it.contactId != null }
            }
            .flatMap { debtors ->
                if (debtors.isEmpty()) {
                    return@flatMap Single.fromCallable { debtors to emptyList<ContactsItemModel>() }
                }
                repository.getContacts()
                    .map { contacts -> debtors to contacts }
            }
            .map { (debtors, contacts) ->
                val updateItems = mutableListOf<DebtorModel>()
                debtors.forEach { debtor ->
                    var contactItem = contacts.firstOrNull { contact ->
                        contact.name == debtor.name
                    }
                    if (contactItem == null) {
                        contactItem = contacts.firstOrNull { contact ->
                            contact.id == debtor.contactId
                        }
                    }
                    contactItem?.let { contact ->
                        updateItems.add(
                            debtor.copy(
                                contactId = contact.id,
                                name = contact.name,
                                avatarUrl = contact.avatarUrl
                            )
                        )
                    }
                }
                updateItems
            }
            .flatMapCompletable { updateItems ->
                if (updateItems.isEmpty()) return@flatMapCompletable Completable.complete()
                repository.updateDebtors(updateItems)
            }
            .andThen(
                repository.setContactsSynced(true)
            )
}
