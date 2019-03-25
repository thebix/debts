package debts.home.usecase

import debts.home.repository.DebtsRepository
import io.reactivex.Completable
import io.reactivex.Single

class SyncDebtorsWithContactsUseCase(
    private val repository: DebtsRepository
) {

    /**
     * forceSync ignores preferences check
     */
    fun execute(forceSync: Boolean = false): Completable =
        (if (forceSync) {
            Single.fromCallable { true }
        } else {
            repository.isContactsSynced()
                .map { it.not() }
        })
            .filter { it }
            .flatMapSingle {
                repository.getDebtors()
            }
            .map { items ->
                items.filter { it.contactId != null }
            }
            .flatMap { debtors ->
                repository.getContacts()
                    .map { contacts -> debtors to contacts }

            }
            .map { (debtors, contacts) ->
                val updateItems = mutableListOf<DebtorModel>()
                debtors.forEach { debtor ->
                    contacts.firstOrNull { contact ->
                        contact.id == debtor.contactId
                    }?.let { contact ->
                        updateItems.add(
                            debtor.copy(
                                name = contact.name,
                                avatarUrl = contact.avatarUrl
                            )
                        )
                    }
                }
                updateItems
            }
            .flatMapCompletable { updateItems ->
                repository.updateDebtors(updateItems)

            }.andThen(
                repository.setContactsSynced(true)
            )
}

