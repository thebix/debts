package debts.home.usecase

import debts.home.repository.DebtsRepository
import io.reactivex.Single

class GetContactsUseCase(
    private val repository: DebtsRepository
) {

    fun execute(): Single<List<ContactsItemModel>> {
        return repository.getContacts()
    }
}
