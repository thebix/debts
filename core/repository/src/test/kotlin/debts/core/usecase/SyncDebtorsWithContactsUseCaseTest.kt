package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.repository.data.ContactsItemModel
import debts.core.repository.data.DebtorModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SyncDebtorsWithContactsUseCaseTest {

    private val repository: DebtsRepository = mockk(relaxed = true)
    private val useCase = SyncDebtorsWithContactsUseCase(repository)

    private fun debtor(id: Long, name: String, contactId: Long? = null) =
        DebtorModel(id = id, name = name, contactId = contactId, avatarUrl = "")

    private fun contact(id: Long, name: String, avatarUrl: String = "avatar_$id") =
        ContactsItemModel(id = id, name = name, avatarUrl = avatarUrl)

    @Test
    fun `skips sync when already synced and forceSync is false`() = runTest {
        coEvery { repository.isContactsSynced() } returns true

        useCase.execute(forceSync = false)

        coVerify(exactly = 0) { repository.getDebtors() }
        coVerify { repository.setContactsSynced(true) }
    }

    @Test
    fun `performs sync when not yet synced`() = runTest {
        coEvery { repository.isContactsSynced() } returns false
        coEvery { repository.getDebtors() } returns listOf(debtor(1, "Alice", contactId = 10))
        coEvery { repository.getContacts() } returns listOf(contact(10, "Alice"))

        useCase.execute(forceSync = false)

        coVerify { repository.getDebtors() }
    }

    @Test
    fun `performs sync when forceSync is true regardless of preference`() = runTest {
        coEvery { repository.isContactsSynced() } returns true
        coEvery { repository.getDebtors() } returns emptyList()

        useCase.execute(forceSync = true)

        coVerify { repository.getDebtors() }
    }

    @Test
    fun `matches contact by name and updates debtor`() = runTest {
        coEvery { repository.isContactsSynced() } returns false
        coEvery { repository.getDebtors() } returns listOf(
            debtor(1, "Alice", contactId = 10)
        )
        coEvery { repository.getContacts() } returns listOf(
            contact(10, "Alice", avatarUrl = "new_avatar")
        )

        useCase.execute()

        coVerify {
            repository.updateDebtors(match { items ->
                items.size == 1 &&
                        items[0].name == "Alice" &&
                        items[0].avatarUrl == "new_avatar"
            })
        }
    }

    @Test
    fun `matches contact by id when name does not match`() = runTest {
        coEvery { repository.isContactsSynced() } returns false
        coEvery { repository.getDebtors() } returns listOf(
            debtor(1, "Old Name", contactId = 42)
        )
        coEvery { repository.getContacts() } returns listOf(
            contact(42, "New Name", avatarUrl = "avatar_42")
        )

        useCase.execute()

        coVerify {
            repository.updateDebtors(match { items ->
                items.size == 1 &&
                        items[0].contactId == 42L &&
                        items[0].name == "New Name"
            })
        }
    }

    @Test
    fun `skips debtors without contactId`() = runTest {
        coEvery { repository.isContactsSynced() } returns false
        coEvery { repository.getDebtors() } returns listOf(
            debtor(1, "Alice", contactId = null)
        )

        useCase.execute()

        coVerify(exactly = 0) { repository.getContacts() }
        coVerify(exactly = 0) { repository.updateDebtors(any()) }
    }

    @Test
    fun `does not call updateDebtors when no contacts match`() = runTest {
        coEvery { repository.isContactsSynced() } returns false
        coEvery { repository.getDebtors() } returns listOf(
            debtor(1, "Alice", contactId = 10)
        )
        coEvery { repository.getContacts() } returns listOf(
            contact(99, "Someone Else")
        )

        useCase.execute()

        coVerify(exactly = 0) { repository.updateDebtors(any()) }
    }

    @Test
    fun `always calls setContactsSynced at the end`() = runTest {
        coEvery { repository.isContactsSynced() } returns true

        useCase.execute(forceSync = false)

        coVerify { repository.setContactsSynced(true) }
    }
}
