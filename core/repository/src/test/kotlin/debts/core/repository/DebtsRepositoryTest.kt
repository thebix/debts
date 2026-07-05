package debts.core.repository

import app.cash.turbine.test
import debts.core.common.android.prefs.Preferences
import debts.core.db.DebtEntity
import debts.core.db.DebtorEntity
import debts.core.db.DebtsDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DebtsRepositoryTest {

    private val dao: DebtsDao = mockk()
    private val preferences: Preferences = mockk(relaxed = true)
    private val contentResolver: android.content.ContentResolver = mockk()
    private val repository = DebtsRepository(contentResolver, dao, preferences)

    // region observeDebtors / getDebtors

    @Test
    fun `observeDebtors maps entities to models`() = runTest {
        val entity = DebtorEntity(1L, 10L, "Alice", "avatar", "", "")
        every { dao.observeDebtors() } returns flowOf(listOf(entity))

        repository.observeDebtors().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(1L, items[0].id)
            assertEquals("Alice", items[0].name)
            assertEquals(10L, items[0].contactId)
            awaitComplete()
        }
    }

    @Test
    fun `observeDebtors emits empty list when dao returns empty`() = runTest {
        every { dao.observeDebtors() } returns flowOf(emptyList())

        repository.observeDebtors().test {
            assertEquals(emptyList(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getDebtors returns first emission from flow`() = runTest {
        val entity = DebtorEntity(2L, null, "Bob", "", "", "")
        every { dao.observeDebtors() } returns flowOf(listOf(entity))

        val result = repository.getDebtors()

        assertEquals(1, result.size)
        assertEquals("Bob", result[0].name)
        assertNull(result[0].contactId)
    }

    // endregion

    // region observeDebtor

    @Test
    fun `observeDebtor maps non-null entity`() = runTest {
        val entity = DebtorEntity(3L, null, "Carol", "url", "", "")
        every { dao.observeDebtor(3L) } returns flowOf(entity)

        repository.observeDebtor(3L).test {
            val model = awaitItem()
            assertEquals(3L, model?.id)
            assertEquals("Carol", model?.name)
            awaitComplete()
        }
    }

    @Test
    fun `observeDebtor emits null when entity is null`() = runTest {
        every { dao.observeDebtor(99L) } returns flowOf(null)

        repository.observeDebtor(99L).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    // endregion

    // region observeDebts / getDebts

    @Test
    fun `observeDebts with debtorId=0 returns all debts mapped`() = runTest {
        val entity = DebtEntity(1L, 5L, 100.0, "USD", 1000L, "coffee")
        every { dao.observeDebts() } returns flowOf(listOf(entity))

        repository.observeDebts(0L).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(1L, items[0].id)
            assertEquals(5L, items[0].debtorId)
            assertEquals(100.0, items[0].amount)
            awaitComplete()
        }
    }

    @Test
    fun `observeDebts with non-zero debtorId filters by debtorId`() = runTest {
        val entity = DebtEntity(2L, 7L, 50.0, "EUR", 2000L, "lunch")
        every { dao.observeDebts(7L) } returns flowOf(listOf(entity))

        repository.observeDebts(7L).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(7L, items[0].debtorId)
            awaitComplete()
        }
    }

    @Test
    fun `getDebts returns first emission from flow`() = runTest {
        every { dao.observeDebts() } returns flowOf(emptyList())

        val result = repository.getDebts()

        assertEquals(emptyList(), result)
    }

    // endregion

    // region mutations

    @Test
    fun `createDebtor inserts and returns id`() = runTest {
        coEvery { dao.insertDebtor(any()) } returns 42L

        val id = repository.createDebtor("Dave", null, "")

        assertEquals(42L, id)
        coVerify { dao.insertDebtor(match { it.name == "Dave" }) }
    }

    @Test
    fun `saveDebt inserts and returns id`() = runTest {
        coEvery { dao.insertDebt(any()) } returns 10L

        val id = repository.saveDebt(1L, 200.0, "USD", "note", 9999L)

        assertEquals(10L, id)
        coVerify { dao.insertDebt(match { it.amount == 200.0 && it.debtorId == 1L }) }
    }

    @Test
    fun `removeDebtor delegates to dao deleteDebtor`() = runTest {
        coEvery { dao.deleteDebtor(5L) } returns Unit

        repository.removeDebtor(5L)

        coVerify { dao.deleteDebtor(5L) }
    }

    @Test
    fun `removeDebt delegates to dao deleteDebt`() = runTest {
        coEvery { dao.deleteDebt(3L) } returns Unit

        repository.removeDebt(3L)

        coVerify { dao.deleteDebt(3L) }
    }

    @Test
    fun `clearDebts delegates to dao clearAllDebts`() = runTest {
        coEvery { dao.clearAllDebts(7L) } returns Unit

        repository.clearDebts(7L)

        coVerify { dao.clearAllDebts(7L) }
    }

    @Test
    fun `updateDebtsCurrency reads currency from prefs and delegates to dao`() = runTest {
        every { preferences.getString(any(), any()) } returns "EUR"
        coEvery { dao.updateDebtsCurrency("EUR") } returns Unit

        repository.updateDebtsCurrency()

        coVerify { dao.updateDebtsCurrency("EUR") }
    }

    // endregion

    // region preferences

    @Test
    fun `isContactsSynced reads from preferences`() = runTest {
        every { preferences.getBoolean("PREFS_IS_CONTACT_SYNCED", false) } returns true

        assertEquals(true, repository.isContactsSynced())
    }

    @Test
    fun `getCurrency reads from preferences`() = runTest {
        every { preferences.getString("preference_main_settings_currency_custom", "") } returns "USD"

        assertEquals("USD", repository.getCurrency())
    }

    @Test
    fun `observeCurrency emits value from preferences observable`() = runTest {
        every { preferences.observeString("preference_main_settings_currency_custom", "") } returns
                Observable.just("GBP")

        repository.observeCurrency().test {
            assertEquals("GBP", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observeSortType maps string to SortType`() = runTest {
        every { preferences.observeString("PREFS_SORT_KEY", SortType.NOTHING.name) } returns
                Observable.just(SortType.AMOUNT_DESC.name)

        repository.observeSortType().test {
            assertEquals(SortType.AMOUNT_DESC, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `setSortType writes to preferences`() {
        repository.setSortType(SortType.NAME_ASC)

        verify { preferences.putString("PREFS_SORT_KEY", "NAME_ASC") }
    }

    // endregion
}
