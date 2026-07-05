package debts.core.usecase

import app.cash.turbine.test
import debts.core.repository.DebtsRepository
import debts.core.repository.data.DebtModel
import debts.core.repository.data.DebtorModel
import debts.core.usecase.data.TabTypes
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveDebtorsListItemsUseCaseTest {

    private val repository: DebtsRepository = mockk()
    private val useCase = ObserveDebtorsListItemsUseCase(repository)

    private fun debtor(id: Long, name: String) =
        DebtorModel(id = id, name = name, contactId = null, avatarUrl = "")

    private fun debt(id: Long, debtorId: Long, amount: Double, currency: String = "USD", date: Long = 1000L) =
        DebtModel(id = id, debtorId = debtorId, amount = amount, currency = currency, date = date, comment = "")

    @Test
    fun `TabTypes All returns all debtors`() = runTest {
        every { repository.observeDebtors() } returns flowOf(listOf(debtor(1, "Alice"), debtor(2, "Bob")))
        every { repository.observeDebts() } returns flowOf(emptyList())
        every { repository.observeCurrency() } returns flowOf("EUR")

        useCase.execute(TabTypes.All).test {
            val items = awaitItem()
            assertEquals(2, items.size)
            awaitComplete()
        }
    }

    @Test
    fun `TabTypes Debtors filters out creditors`() = runTest {
        every { repository.observeDebtors() } returns flowOf(listOf(debtor(1, "Alice"), debtor(2, "Bob")))
        every { repository.observeDebts() } returns flowOf(
            listOf(
                debt(1, 1L, 50.0), // Alice owes → positive amount → Debtor
                debt(2, 2L, -30.0) // Bob lent → negative → Creditor
            )
        )
        every { repository.observeCurrency() } returns flowOf("USD")

        useCase.execute(TabTypes.Debtors).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Alice", items[0].name)
            awaitComplete()
        }
    }

    @Test
    fun `TabTypes Creditors filters out debtors`() = runTest {
        every { repository.observeDebtors() } returns flowOf(listOf(debtor(1, "Alice"), debtor(2, "Bob")))
        every { repository.observeDebts() } returns flowOf(
            listOf(
                debt(1, 1L, 50.0),
                debt(2, 2L, -30.0)
            )
        )
        every { repository.observeCurrency() } returns flowOf("USD")

        useCase.execute(TabTypes.Creditors).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Bob", items[0].name)
            awaitComplete()
        }
    }

    @Test
    fun `amount is sum of all debts for debtor`() = runTest {
        every { repository.observeDebtors() } returns flowOf(listOf(debtor(1, "Alice")))
        every { repository.observeDebts() } returns flowOf(
            listOf(
                debt(1, 1L, 100.0),
                debt(2, 1L, 50.0),
                debt(3, 1L, -25.0)
            )
        )
        every { repository.observeCurrency() } returns flowOf("USD")

        useCase.execute(TabTypes.All).test {
            val item = awaitItem()[0]
            assertEquals(125.0, item.amount)
            awaitComplete()
        }
    }

    @Test
    fun `currency is taken from last debt when available`() = runTest {
        every { repository.observeDebtors() } returns flowOf(listOf(debtor(1, "Alice")))
        every { repository.observeDebts() } returns flowOf(
            listOf(
                debt(1, 1L, 10.0, currency = "EUR", date = 2000L),
                debt(2, 1L, 10.0, currency = "GBP", date = 1000L)
            )
        )
        every { repository.observeCurrency() } returns flowOf("USD")

        useCase.execute(TabTypes.All).test {
            val item = awaitItem()[0]
            assertEquals("EUR", item.currency) // most recent debt (date=2000)
            awaitComplete()
        }
    }

    @Test
    fun `currency falls back to default when debtor has no debts`() = runTest {
        every { repository.observeDebtors() } returns flowOf(listOf(debtor(1, "Alice")))
        every { repository.observeDebts() } returns flowOf(emptyList())
        every { repository.observeCurrency() } returns flowOf("CHF")

        useCase.execute(TabTypes.All).test {
            val item = awaitItem()[0]
            assertEquals("CHF", item.currency)
            awaitComplete()
        }
    }

    @Test
    fun `debts from other debtors are not included in amount`() = runTest {
        every { repository.observeDebtors() } returns flowOf(listOf(debtor(1, "Alice")))
        every { repository.observeDebts() } returns flowOf(
            listOf(
                debt(1, 1L, 100.0),
                debt(2, 99L, 999.0) // belongs to debtor 99, not Alice
            )
        )
        every { repository.observeCurrency() } returns flowOf("USD")

        useCase.execute(TabTypes.All).test {
            val item = awaitItem()[0]
            assertEquals(100.0, item.amount)
            awaitComplete()
        }
    }

    @Test
    fun `zero-debt debtor appears in All and Debtors tabs`() = runTest {
        every { repository.observeDebtors() } returns flowOf(listOf(debtor(1, "Alice")))
        every { repository.observeDebts() } returns flowOf(emptyList())
        every { repository.observeCurrency() } returns flowOf("USD")

        useCase.execute(TabTypes.All).test {
            assertTrue(awaitItem().isNotEmpty())
            awaitComplete()
        }
        useCase.execute(TabTypes.Debtors).test {
            assertTrue(awaitItem().isNotEmpty()) // amount == 0 → >= 0 → Debtors
            awaitComplete()
        }
    }
}
