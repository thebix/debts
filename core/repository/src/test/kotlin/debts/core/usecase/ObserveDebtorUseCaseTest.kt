package debts.core.usecase

import app.cash.turbine.test
import debts.core.repository.DebtsRepository
import debts.core.repository.data.DebtModel
import debts.core.repository.data.DebtorModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveDebtorUseCaseTest {

    private val repository: DebtsRepository = mockk()
    private val useCase = ObserveDebtorUseCase(repository)

    @Test
    fun `combines debtor debts and currency into DebtorDetailsModel`() = runTest {
        val debtor = DebtorModel(1L, "Alice", null, "avatar.png")
        every { repository.observeDebtor(1L) } returns flowOf(debtor)
        every { repository.observeDebts(1L) } returns flowOf(listOf(
            DebtModel(1L, 1L, 100.0, "USD", 0L, ""),
            DebtModel(2L, 1L, 50.0, "USD", 0L, "")
        ))
        every { repository.observeCurrency() } returns flowOf("EUR")

        useCase.execute(1L).test {
            val model = awaitItem()
            assertEquals("Alice", model.name)
            assertEquals(150.0, model.amount)
            assertEquals("EUR", model.currency)
            assertEquals("avatar.png", model.avatarUrl)
            awaitComplete()
        }
    }

    @Test
    fun `amount is zero when debtor has no debts`() = runTest {
        val debtor = DebtorModel(1L, "Bob", null, "")
        every { repository.observeDebtor(1L) } returns flowOf(debtor)
        every { repository.observeDebts(1L) } returns flowOf(emptyList())
        every { repository.observeCurrency() } returns flowOf("USD")

        useCase.execute(1L).test {
            assertEquals(0.0, awaitItem().amount)
            awaitComplete()
        }
    }

    @Test
    fun `negative amounts are summed correctly`() = runTest {
        val debtor = DebtorModel(1L, "Carol", null, "")
        every { repository.observeDebtor(1L) } returns flowOf(debtor)
        every { repository.observeDebts(1L) } returns flowOf(listOf(
            DebtModel(1L, 1L, -200.0, "USD", 0L, "")
        ))
        every { repository.observeCurrency() } returns flowOf("USD")

        useCase.execute(1L).test {
            assertEquals(-200.0, awaitItem().amount)
            awaitComplete()
        }
    }
}
