package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.repository.data.ContactsItemModel
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle

class GetContactsUseCase(
    private val repository: DebtsRepository
) {

    fun execute(): Single<List<ContactsItemModel>> = rxSingle { repository.getContacts() }
}
