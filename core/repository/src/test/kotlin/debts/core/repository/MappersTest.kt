package debts.core.repository

import debts.core.db.DebtEntity
import debts.core.db.DebtorEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MappersTest {

    @Test
    fun `DebtorEntity maps to DebtorModel`() {
        val entity = DebtorEntity(
            id = 1L,
            contactId = 42L,
            name = "John Doe",
            avatarUrl = "https://example.com/avatar.jpg",
            email = "",
            phone = "",
        )

        val model = entity.toDebtorModel()

        assertEquals(1L, model.id)
        assertEquals("John Doe", model.name)
        assertEquals(42L, model.contactId)
        assertEquals("https://example.com/avatar.jpg", model.avatarUrl)
    }

    @Test
    fun `DebtorEntity with null contactId maps to DebtorModel with null contactId`() {
        val entity = DebtorEntity(
            id = 2L,
            contactId = null,
            name = "Jane Doe",
            avatarUrl = "",
            email = "",
            phone = "",
        )

        val model = entity.toDebtorModel()

        assertEquals(2L, model.id)
        assertNull(model.contactId)
    }

    @Test
    fun `DebtEntity maps to DebtModel`() {
        val entity = DebtEntity(
            id = 10L,
            debtorId = 1L,
            amount = 150.5,
            currency = "USD",
            date = 1_700_000_000L,
            comment = "Lunch",
        )

        val model = entity.toDebtModel()

        assertEquals(10L, model.id)
        assertEquals(1L, model.debtorId)
        assertEquals(150.5, model.amount)
        assertEquals("USD", model.currency)
        assertEquals(1_700_000_000L, model.date)
        assertEquals("Lunch", model.comment)
    }

    @Test
    fun `DebtorModel maps to DebtorEntity with empty legacy fields`() {
        val model = debts.core.repository.data.DebtorModel(
            id = 5L,
            name = "Alice",
            contactId = 100L,
            avatarUrl = "https://example.com/alice.jpg",
        )

        val entity = model.toDebtorEntity()

        assertEquals(5L, entity.id)
        assertEquals(100L, entity.contactId)
        assertEquals("Alice", entity.name)
        assertEquals("https://example.com/alice.jpg", entity.avatarUrl)
        assertEquals("", entity.email)
        assertEquals("", entity.phone)
    }
}
