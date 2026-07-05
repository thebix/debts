package debts.core.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class DebtsDaoTest {

    private lateinit var db: DebtsDatabase
    private lateinit var dao: DebtsDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DebtsDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
        dao = db.debtsDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // region DebtorEntity

    @Test
    fun `insertDebtor returns generated id`() = runTest {
        val id = dao.insertDebtor(debtorEntity(id = 0))

        assertNotEquals(0L, id)
    }

    @Test
    fun `observeDebtors emits inserted debtor`() = runTest {
        val debtor = debtorEntity(id = 0, name = "Alice")
        dao.insertDebtor(debtor)

        dao.observeDebtors().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Alice", items.first().name)
            cancel()
        }
    }

    @Test
    fun `observeDebtors emits empty list when no rows`() = runTest {
        dao.observeDebtors().test {
            assertTrue(awaitItem().isEmpty())
            cancel()
        }
    }

    @Test
    fun `observeDebtor emits null when row does not exist`() = runTest {
        dao.observeDebtor(id = 999L).test {
            assertNull(awaitItem())
            cancel()
        }
    }

    @Test
    fun `updateDebtor by entity changes stored values`() = runTest {
        val id = dao.insertDebtor(debtorEntity(id = 0, name = "Bob"))
        dao.updateDebtor(debtorEntity(id = id, name = "Bobby"))

        dao.observeDebtors().test {
            assertEquals("Bobby", awaitItem().first().name)
            cancel()
        }
    }

    @Test
    fun `deleteDebtor by id removes the row`() = runTest {
        val id = dao.insertDebtor(debtorEntity(id = 0))
        dao.deleteDebtor(id)

        dao.observeDebtors().test {
            assertTrue(awaitItem().isEmpty())
            cancel()
        }
    }

    @Test
    fun `updateDebtors updates multiple rows in one transaction`() = runTest {
        val id1 = dao.insertDebtor(debtorEntity(id = 0, name = "A", avatar = "old"))
        val id2 = dao.insertDebtor(debtorEntity(id = 0, name = "B", avatar = "old"))

        dao.updateDebtors(
            listOf(
                debtorEntity(id = id1, name = "A", avatar = "new1"),
                debtorEntity(id = id2, name = "B", avatar = "new2"),
            )
        )

        dao.observeDebtors().test {
            val items = awaitItem().sortedBy { it.id }
            assertEquals("new1", items[0].avatarUrl)
            assertEquals("new2", items[1].avatarUrl)
            cancel()
        }
    }

    // endregion

    // region DebtEntity

    @Test
    fun `insertDebt returns generated id`() = runTest {
        val debtorId = dao.insertDebtor(debtorEntity(id = 0))

        val id = dao.insertDebt(debtEntity(id = 0, debtorId = debtorId))

        assertNotEquals(0L, id)
    }

    @Test
    fun `observeDebts emits inserted debt`() = runTest {
        val debtorId = dao.insertDebtor(debtorEntity(id = 0))
        dao.insertDebt(debtEntity(id = 0, debtorId = debtorId, amount = 42.0))

        dao.observeDebts().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(42.0, items.first().amount)
            cancel()
        }
    }

    @Test
    fun `observeDebts filtered by debtorId returns only matching rows`() = runTest {
        val debtorId1 = dao.insertDebtor(debtorEntity(id = 0, name = "A"))
        val debtorId2 = dao.insertDebtor(debtorEntity(id = 0, name = "B"))
        dao.insertDebt(debtEntity(id = 0, debtorId = debtorId1, amount = 10.0))
        dao.insertDebt(debtEntity(id = 0, debtorId = debtorId2, amount = 20.0))

        dao.observeDebts(debtorId = debtorId1).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(10.0, items.first().amount)
            cancel()
        }
    }

    @Test
    fun `getDebt returns the correct entity`() = runTest {
        val debtorId = dao.insertDebtor(debtorEntity(id = 0))
        val debtId = dao.insertDebt(debtEntity(id = 0, debtorId = debtorId, amount = 99.0))

        val debt = dao.getDebt(debtId)

        assertEquals(99.0, debt.amount)
    }

    @Test
    fun `deleteDebt by id removes the row`() = runTest {
        val debtorId = dao.insertDebtor(debtorEntity(id = 0))
        val debtId = dao.insertDebt(debtEntity(id = 0, debtorId = debtorId))
        dao.deleteDebt(debtId)

        dao.observeDebts().test {
            assertTrue(awaitItem().isEmpty())
            cancel()
        }
    }

    @Test
    fun `clearAllDebts removes all debts for debtor`() = runTest {
        val debtorId = dao.insertDebtor(debtorEntity(id = 0))
        dao.insertDebt(debtEntity(id = 0, debtorId = debtorId, amount = 1.0))
        dao.insertDebt(debtEntity(id = 0, debtorId = debtorId, amount = 2.0))
        dao.clearAllDebts(debtorId)

        dao.observeDebts(debtorId = debtorId).test {
            assertTrue(awaitItem().isEmpty())
            cancel()
        }
    }

    @Test
    fun `updateDebtsCurrency updates currency on all debts`() = runTest {
        val debtorId = dao.insertDebtor(debtorEntity(id = 0))
        dao.insertDebt(debtEntity(id = 0, debtorId = debtorId, currency = "USD"))
        dao.insertDebt(debtEntity(id = 0, debtorId = debtorId, currency = "USD"))

        dao.updateDebtsCurrency("EUR")

        dao.observeDebts().test {
            val items = awaitItem()
            assertTrue(items.all { it.currency == "EUR" })
            cancel()
        }
    }

    // endregion

    // region helpers

    private fun debtorEntity(
        id: Long,
        name: String = "Name",
        avatar: String = "",
    ) = DebtorEntity(
        id = id,
        contactId = null,
        name = name,
        avatarUrl = avatar,
        email = "",
        phone = "",
    )

    private fun debtEntity(
        id: Long,
        debtorId: Long,
        amount: Double = 0.0,
        currency: String = "USD",
    ) = DebtEntity(
        id = id,
        debtorId = debtorId,
        amount = amount,
        currency = currency,
        date = 0L,
        comment = "",
    )

    // endregion
}
