package debts.feature.details

import app.cash.turbine.test
import debts.core.repository.DebtsRepository
import debts.core.repository.data.DebtModel
import debts.core.usecase.AddDebtUseCase
import debts.core.usecase.ClearHistoryUseCase
import debts.core.usecase.GetDebtUseCase
import debts.core.usecase.GetShareDebtorContentUseCase
import debts.core.usecase.ObserveDebtorUseCase
import debts.core.usecase.ObserveDebtsUseCase
import debts.core.usecase.RemoveDebtUseCase
import debts.core.usecase.RemoveDebtorUseCase
import debts.core.usecase.UpdateDebtUseCase
import debts.core.usecase.data.DebtItemModel
import debts.core.usecase.data.DebtorDetailsModel
import debts.feature.adddebt.DebtLayoutData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val DEBTOR_ID = 7L

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    private val clearHistoryUseCase: ClearHistoryUseCase = mockk(relaxed = true)
    private val addDebtUseCase: AddDebtUseCase = mockk(relaxed = true)
    private val observeDebtorUseCase: ObserveDebtorUseCase = mockk()
    private val observeDebtsUseCase: ObserveDebtsUseCase = mockk()
    private val removeDebtUseCase: RemoveDebtUseCase = mockk(relaxed = true)
    private val getDebtUseCase: GetDebtUseCase = mockk()
    private val updateDebtUseCase: UpdateDebtUseCase = mockk(relaxed = true)
    private val removeDebtorUseCase: RemoveDebtorUseCase = mockk(relaxed = true)
    private val getShareDebtorContentUseCase: GetShareDebtorContentUseCase = mockk()
    private val repository: DebtsRepository = mockk(relaxed = true)

    private val debtor = MutableStateFlow(DebtorDetailsModel("Anna", -70.0, "$", "content://avatar"))
    private val debts = MutableStateFlow(listOf(debt(1, 30.0, date = 100), debt(2, -100.0, date = 300)))

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        every { observeDebtorUseCase.execute(DEBTOR_ID) } returns debtor
        every { observeDebtsUseCase.execute(DEBTOR_ID) } returns debts
        coEvery { repository.getCurrency() } returns "$"
        coEvery { getDebtUseCase.execute(2) } returns DebtModel(2, DEBTOR_ID, -100.0, "€", 300, "lunch")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region State

    @Test
    fun `state holds the debtor with an absolute amount and the history newest first`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals("Anna", name)
            assertEquals("content://avatar", avatarUrl)
            assertEquals(70.0, amount, 0.0)
            assertEquals("$", currency)
            assertEquals(listOf(2L, 1L), this.debts.map { it.id })
        }
    }

    @Test
    fun `state follows changes of the debtor and of the history`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        debtor.value = DebtorDetailsModel("Anna B", 15.0, "€", "")
        debts.value = emptyList()
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals("Anna B", name)
            assertEquals(15.0, amount, 0.0)
            assertEquals("€", currency)
            assertTrue(this.debts.isEmpty())
        }
    }

    @Test
    fun `history is shown even if the debtor never arrives`() = runTest {
        every { observeDebtorUseCase.execute(DEBTOR_ID) } returns MutableSharedFlow()
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.name)
        assertEquals(2, viewModel.uiState.value.debts.size)
    }

    @Test
    fun `failing flows are logged and leave the default state`() = runTest {
        every { observeDebtorUseCase.execute(DEBTOR_ID) } returns flow { error("debtor") }
        every { observeDebtsUseCase.execute(DEBTOR_ID) } returns flow { error("debts") }
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(DetailsUiState(), viewModel.uiState.value)
    }

    // endregion

    // region Add and edit

    @Test
    fun `change click asks to show the add dialog for this debtor`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onChangeClicked()
            assertEquals(DetailsEvent.ShowAddDebtDialog("Anna", "content://avatar"), awaitItem())
        }
    }

    @Test
    fun `confirming a new debt adds it to this debtor in the app currency`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onDebtDialogConfirmed(layoutData(amount = -12.5, comment = "taxi", date = 500))
            assertEquals(DetailsEvent.DebtAdded, awaitItem())
        }
        coVerify { addDebtUseCase.execute(DEBTOR_ID, null, "", -12.5, "$", "taxi", 500) }
    }

    @Test
    fun `confirming a zero amount saves nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onDebtDialogConfirmed(layoutData(amount = 0.0))
            viewModel.onDebtDialogConfirmed(layoutData(amount = 0.0, existingDebtId = 2))
            assertEquals(DetailsEvent.EmptyDebtAmount, awaitItem())
            assertEquals(DetailsEvent.EmptyDebtAmount, awaitItem())
        }
        coVerify(exactly = 0) { addDebtUseCase.execute(any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { updateDebtUseCase.execute(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `debt click asks to show the edit dialog with the stored debt`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onDebtClicked(2)
            assertEquals(
                DetailsEvent.ShowEditDebtDialog(
                    name = "Anna",
                    avatarUrl = "content://avatar",
                    debtId = 2,
                    amount = -100.0,
                    comment = "lunch",
                    date = 300,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `confirming an edit keeps the debtor and the currency of the stored debt`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onDebtDialogConfirmed(layoutData(amount = 40.0, comment = "dinner", date = 900, existingDebtId = 2))
            assertEquals(DetailsEvent.DebtChanged, awaitItem())
        }
        coVerify {
            updateDebtUseCase.execute(
                id = 2,
                debtorId = DEBTOR_ID,
                amount = 40.0,
                date = 900,
                currency = "€",
                comment = "dinner",
            )
        }
    }

    @Test
    fun `a failing use case sends no event and does not break the view model`() = runTest {
        coEvery { addDebtUseCase.execute(any(), any(), any(), any(), any(), any(), any()) } throws
            IllegalStateException("db")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onDebtDialogConfirmed(layoutData(amount = 5.0))
            advanceUntilIdle()
            expectNoEvents()

            viewModel.onChangeClicked()
            assertEquals(DetailsEvent.ShowAddDebtDialog("Anna", "content://avatar"), awaitItem())
        }
    }

    // endregion

    // region Remove, clear and share

    @Test
    fun `confirmed removal of a debt removes that debt`() = runTest {
        val viewModel = createViewModel()
        viewModel.onRemoveDebtConfirmed(2)
        advanceUntilIdle()

        coVerify { removeDebtUseCase.execute(2) }
    }

    @Test
    fun `confirmed clear wipes the history of this debtor`() = runTest {
        val viewModel = createViewModel()
        viewModel.onClearHistoryConfirmed()
        advanceUntilIdle()

        coVerify { clearHistoryUseCase.execute(DEBTOR_ID) }
    }

    @Test
    fun `confirmed removal of the debtor removes it and closes the screen`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onRemoveDebtorConfirmed()
            assertEquals(DetailsEvent.Close, awaitItem())
        }
        coVerify { removeDebtorUseCase.execute(DEBTOR_ID) }
    }

    @Test
    fun `the screen stays open when removing the debtor fails`() = runTest {
        coEvery { removeDebtorUseCase.execute(DEBTOR_ID) } throws IllegalStateException("db")
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onRemoveDebtorConfirmed()
            advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `share sends the text built from the templates`() = runTest {
        coEvery { getShareDebtorContentUseCase.execute(DEBTOR_ID, "borrowed", "lent") } returns "Hey"
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onShareClicked("borrowed", "lent")
            assertEquals(DetailsEvent.ShareDebtor("Hey"), awaitItem())
        }
    }

    // endregion

    private fun createViewModel() = DetailsViewModel(
        debtorId = DEBTOR_ID,
        clearHistoryUseCase = clearHistoryUseCase,
        addDebtUseCase = addDebtUseCase,
        observeDebtorUseCase = observeDebtorUseCase,
        observeDebtsUseCase = observeDebtsUseCase,
        removeDebtUseCase = removeDebtUseCase,
        getDebtUseCase = getDebtUseCase,
        updateDebtUseCase = updateDebtUseCase,
        removeDebtorUseCase = removeDebtorUseCase,
        getShareDebtorContentUseCase = getShareDebtorContentUseCase,
        repository = repository,
    )

    private fun debt(id: Long, amount: Double, date: Long) = DebtItemModel(id, amount, "$", date, "")

    private fun layoutData(
        amount: Double,
        comment: String = "",
        date: Long = 0,
        existingDebtId: Long? = null,
    ) = DebtLayoutData(
        contactId = null,
        name = "Anna",
        amount = amount,
        comment = comment,
        date = date,
        existingDebtId = existingDebtId,
    )
}
