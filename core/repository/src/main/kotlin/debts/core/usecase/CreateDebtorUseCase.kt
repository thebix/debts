package debts.core.usecase

import debts.core.repository.DebtsRepository

class CreateDebtorUseCase(
    private val repository: DebtsRepository,
) {
    suspend fun execute(name: String, contactId: Long?): Long {
        val avatarUrl = if (contactId != null) {
            repository.getContacts().firstOrNull { it.id == contactId }?.avatarUrl ?: ""
        } else {
            ""
        }
        return repository.createDebtor(name, contactId, avatarUrl)
    }
}
