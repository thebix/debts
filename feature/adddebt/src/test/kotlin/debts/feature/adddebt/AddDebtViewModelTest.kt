package debts.feature.adddebt

import app.cash.turbine.test
import debts.feature.contacts.adapter.ContactsItemViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddDebtViewModelTest {

    private lateinit var viewModel: AddDebtViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = AddDebtViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.name)
            assertEquals("", state.amountText)
            assertNull(state.contactId)
            assertFalse(state.isSubtract)
            assertFalse(state.showDatePicker)
        }
    }

    @Test
    fun `init populates fields for add debt`() = runTest {
        val date = 1_000_000L
        viewModel.init(
            name = "Alice",
            avatarUrl = "http://avatar",
            amount = 50.0,
            comment = "coffee",
            dateMs = date,
            contacts = emptyList(),
            existingDebtId = null,
            canChangeDebtor = true,
        )
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Alice", state.name)
            assertEquals("50.0", state.amountText)
            assertFalse(state.isSubtract)
            assertEquals("coffee", state.comment)
            assertEquals(date, state.dateMs)
            assertNull(state.existingDebtId)
            assertTrue(state.canChangeDebtor)
        }
    }

    @Test
    fun `init sets isSubtract for negative amount`() = runTest {
        viewModel.init(
            name = "",
            avatarUrl = "",
            amount = -25.0,
            comment = "",
            dateMs = 0L,
            contacts = emptyList(),
            existingDebtId = 7L,
            canChangeDebtor = false,
        )
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("25.0", state.amountText)
            assertTrue(state.isSubtract)
            assertEquals(7L, state.existingDebtId)
            assertFalse(state.canChangeDebtor)
        }
    }

    @Test
    fun `onNameChanged clears contactId and avatarUrl when contact was selected`() = runTest {
        val contact = ContactsItemViewModel(id = 1L, name = "Bob", avatarUrl = "url")
        viewModel.onContactSelected(contact)
        viewModel.onNameChanged("Bo")
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Bo", state.name)
            assertNull(state.contactId)
            assertEquals("", state.avatarUrl)
        }
    }

    @Test
    fun `onNameChanged preserves avatarUrl when no contact was selected`() = runTest {
        viewModel.onNameChanged("Alice")
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Alice", state.name)
            assertNull(state.contactId)
            assertEquals("", state.avatarUrl)
        }
    }

    @Test
    fun `onContactSelected updates name avatarUrl and contactId`() = runTest {
        val contact = ContactsItemViewModel(id = 42L, name = "Carol", avatarUrl = "http://carol")
        viewModel.onContactSelected(contact)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(42L, state.contactId)
            assertEquals("Carol", state.name)
            assertEquals("http://carol", state.avatarUrl)
        }
    }

    @Test
    fun `onAmountChanged sets error when length exceeds max`() = runTest {
        val longAmount = "1".repeat(17)
        viewModel.onAmountChanged(longAmount)
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.amountError)
        }
    }

    @Test
    fun `onAmountChanged clears error for valid length`() = runTest {
        viewModel.onAmountChanged("123.45")
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.amountError)
        }
    }

    @Test
    fun `buildResult returns positive amount for lent`() = runTest {
        viewModel.onAmountChanged("100.0")
        viewModel.onSubtractChanged(false)
        val result = viewModel.buildResult()
        assertEquals(100.0, result.amount, 0.001)
    }

    @Test
    fun `buildResult returns negative amount for borrowed`() = runTest {
        viewModel.onAmountChanged("50.5")
        viewModel.onSubtractChanged(true)
        val result = viewModel.buildResult()
        assertEquals(-50.5, result.amount, 0.001)
    }

    @Test
    fun `buildResult returns zero for non-numeric amount`() = runTest {
        viewModel.onAmountChanged("abc")
        val result = viewModel.buildResult()
        assertEquals(0.0, result.amount, 0.001)
    }

    @Test
    fun `buildResult trims name and comment`() = runTest {
        viewModel.onNameChanged("  Alice  ")
        viewModel.onCommentChanged("  note  ")
        val result = viewModel.buildResult()
        assertEquals("Alice", result.name)
        assertEquals("note", result.comment)
    }

    @Test
    fun `onCalendarClicked shows date picker`() = runTest {
        viewModel.onCalendarClicked()
        viewModel.uiState.test {
            assertTrue(awaitItem().showDatePicker)
        }
    }

    @Test
    fun `onDateSelected updates date and hides picker`() = runTest {
        viewModel.onCalendarClicked()
        viewModel.onDateSelected(999_999L)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(999_999L, state.dateMs)
            assertFalse(state.showDatePicker)
        }
    }

    @Test
    fun `onDatePickerDismissed hides picker`() = runTest {
        viewModel.onCalendarClicked()
        viewModel.onDatePickerDismissed()
        viewModel.uiState.test {
            assertFalse(awaitItem().showDatePicker)
        }
    }
}
