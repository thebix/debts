package debts.core.usecase

import debts.core.repository.DebtsRepository
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable

class SyncDebtorsWithContactsUseCase(
    private val repository: DebtsRepository
) {

    /**
     * forceSync ignores preferences check
     */
    fun execute(forceSync: Boolean = false): Completable = rxCompletable {
        val shouldSync = if (forceSync) true else !repository.isContactsSynced()
        if (shouldSync) {
            val debtors = repository.getDebtors().filter { it.contactId != null }
            if (debtors.isNotEmpty()) {
                val contacts = repository.getContacts()
                val updateItems = debtors.mapNotNull { debtor ->
                    val contact = contacts.firstOrNull { it.name == debtor.name }
                        ?: contacts.firstOrNull { it.id == debtor.contactId }
                    contact?.let {
                        debtor.copy(contactId = it.id, name = it.name, avatarUrl = it.avatarUrl)
                    }
                }
                if (updateItems.isNotEmpty()) repository.updateDebtors(updateItems)
            }
        }
        repository.setContactsSynced(true)
    }
}
