package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.repository.data.ContactsItemModel

class GetContactsUseCase(
    private val repository: DebtsRepository
) {

    suspend fun execute(): List<ContactsItemModel> = repository.getContacts()
}
