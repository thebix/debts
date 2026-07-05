package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.repository.data.DebtorModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AddDebtUseCaseTest {

    private val repository: DebtsRepository = mockk(relaxed = true)
    private val createDebtorUseCase: CreateDebtorUseCase = mockk()
    private val useCase = AddDebtUseCase(repository, createDebtorUseCase)

    private fun debtor(id: Long, name: String, contactId: Long? = null) =
        DebtorModel(id = id, name = name, contactId = contactId, avatarUrl = "")

    @Test
    fun `uses provided debtorId directly without querying`() = runTest {
        useCase.execute(
            debtorId = 5L, contactId = null, name = "Alice",
            amount = 100.0, currency = "USD", comment = "", date = 0L
        )

        coVerify(exactly = 0) { repository.getDebtors() }
        coVerify { repository.saveDebt(5L, 100.0, "USD", "", 0L) }
    }

    @Test
    fun `finds existing debtor by contactId`() = runTest {
        coEvery { repository.getDebtors() } returns listOf(debtor(3L, "Alice", contactId = 10L))

        useCase.execute(
            debtorId = null, contactId = 10L, name = "Alice",
            amount = 50.0, currency = "EUR", comment = "lunch", date = 1000L
        )

        coVerify(exactly = 0) { createDebtorUseCase.execute(any(), any()) }
        coVerify { repository.saveDebt(3L, 50.0, "EUR", "lunch", 1000L) }
    }

    @Test
    fun `finds existing debtor by name when contactId has no match`() = runTest {
        coEvery { repository.getDebtors() } returns listOf(debtor(4L, "Bob", contactId = null))

        useCase.execute(
            debtorId = null, contactId = null, name = "Bob",
            amount = 20.0, currency = "USD", comment = "", date = 0L
        )

        coVerify(exactly = 0) { createDebtorUseCase.execute(any(), any()) }
        coVerify { repository.saveDebt(4L, 20.0, "USD", "", 0L) }
    }

    @Test
    fun `creates new debtor when no existing match found`() = runTest {
        coEvery { repository.getDebtors() } returns emptyList()
        coEvery { createDebtorUseCase.execute("NewPerson", null) } returns 7L

        useCase.execute(
            debtorId = null, contactId = null, name = "NewPerson",
            amount = 75.0, currency = "USD", comment = "book", date = 5000L
        )

        coVerify { createDebtorUseCase.execute("NewPerson", null) }
        coVerify { repository.saveDebt(7L, 75.0, "USD", "book", 5000L) }
    }

    @Test
    fun `contactId match takes priority over name match`() = runTest {
        coEvery { repository.getDebtors() } returns listOf(
            debtor(1L, "Alice", contactId = 10L),
            debtor(2L, "Bob", contactId = null)
        )

        useCase.execute(
            debtorId = null, contactId = 10L, name = "Bob",
            amount = 10.0, currency = "USD", comment = "", date = 0L
        )

        // Should use debtor 1 (matched by contactId), not debtor 2 (matched by name)
        coVerify { repository.saveDebt(1L, any(), any(), any(), any()) }
    }
}
