package debts.core.usecase

import debts.core.repository.DebtsRepository
import debts.core.repository.data.DebtModel
import debts.core.repository.data.DebtorModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class GetDebtsCsvContentUseCaseTest {

    private val repository: DebtsRepository = mockk()
    private val useCase = GetDebtsCsvContentUseCase(repository)

    @Test
    fun `output starts with CSV header`() = runTest {
        coEvery { repository.getDebtors() } returns emptyList()
        coEvery { repository.getDebts() } returns emptyList()
        coEvery { repository.getCurrency() } returns "USD"

        val result = useCase.execute()

        assertTrue(result.startsWith("Date,\tName,\tAmount,\tCurrency,\tComment"))
    }

    @Test
    fun `each debt produces a row with debtor name`() = runTest {
        val debtor = DebtorModel(id = 1L, name = "Alice", contactId = null, avatarUrl = "")
        val debt = DebtModel(id = 1L, debtorId = 1L, amount = 100.0, currency = "USD", date = 0L, comment = "coffee")
        coEvery { repository.getDebtors() } returns listOf(debtor)
        coEvery { repository.getDebts() } returns listOf(debt)
        coEvery { repository.getCurrency() } returns "USD"

        val result = useCase.execute()
        val lines = result.lines()

        assertTrue(lines.size == 2, "Expected header + 1 row, got ${lines.size}")
        assertTrue(lines[1].contains("Alice"))
        assertTrue(lines[1].contains("100.0"))
        assertTrue(lines[1].contains("coffee"))
    }

    @Test
    fun `debts with unknown debtorId are skipped`() = runTest {
        val debtor = DebtorModel(id = 1L, name = "Alice", contactId = null, avatarUrl = "")
        val orphanDebt = DebtModel(id = 2L, debtorId = 99L, amount = 50.0, currency = "USD", date = 0L, comment = "")
        coEvery { repository.getDebtors() } returns listOf(debtor)
        coEvery { repository.getDebts() } returns listOf(orphanDebt)
        coEvery { repository.getCurrency() } returns "USD"

        val result = useCase.execute()

        assertTrue(result.lines().size == 1, "Orphan debt should not produce a row")
    }

    @Test
    fun `multiple debts produce multiple rows`() = runTest {
        val debtors = listOf(
            DebtorModel(1L, "Alice", null, ""),
            DebtorModel(2L, "Bob", null, "")
        )
        val debts = listOf(
            DebtModel(1L, 1L, 10.0, "USD", 0L, ""),
            DebtModel(2L, 1L, 20.0, "USD", 0L, ""),
            DebtModel(3L, 2L, 30.0, "USD", 0L, "")
        )
        coEvery { repository.getDebtors() } returns debtors
        coEvery { repository.getDebts() } returns debts
        coEvery { repository.getCurrency() } returns "USD"

        val result = useCase.execute()

        assertTrue(result.lines().size == 4, "Expected header + 3 rows")
    }
}
