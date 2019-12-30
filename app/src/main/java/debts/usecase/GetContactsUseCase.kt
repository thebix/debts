package debts.usecase

import debts.repository.DebtsRepository
import io.reactivex.Single

class GetContactsUseCase(
    private val repository: DebtsRepository
) {

    fun execute(): Single<List<ContactsItemModel>> {
        return repository.getContacts()
    }
}
