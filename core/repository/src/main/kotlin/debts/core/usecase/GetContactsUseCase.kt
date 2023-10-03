package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.repository.data.ContactsItemModel
import io.reactivex.Single

class GetContactsUseCase(
    private val repository: DebtsRepository
) {

    fun execute(): Single<List<ContactsItemModel>> {
        return repository.getContacts()
    }
}
