package debts.feature.home

import app.cash.turbine.test
import debts.core.repository.DebtsRepository
import debts.core.repository.SortType
import debts.core.repository.data.ContactsItemModel
import debts.core.usecase.AddDebtUseCase
import debts.core.usecase.GetContactsUseCase
import debts.core.usecase.GetDebtsCsvContentUseCase
import debts.core.usecase.GetShareDebtorContentUseCase
import debts.core.usecase.ObserveDebtorsListItemsUseCase
import debts.core.usecase.RemoveDebtorUseCase
import debts.core.usecase.SyncDebtorsWithContactsUseCase
import debts.core.usecase.UpdateDbDebtsCurrencyUseCase
import debts.core.usecase.data.DebtorsListItemModel
import debts.core.usecase.data.TabTypes
import debts.feature.adddebt.DebtLayoutData
import debts.feature.contacts.adapter.ContactsItemViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val getContactsUseCase: GetContactsUseCase = mockk()
    private val addDebtUseCase: AddDebtUseCase = mockk(relaxed = true)
    private val getDebtsCsvContentUseCase: GetDebtsCsvContentUseCase = mockk()
    private val observeDebtorsListItemsUseCase: ObserveDebtorsListItemsUseCase = mockk()
    private val syncDebtorsWithContactsUseCase: SyncDebtorsWithContactsUseCase = mockk(relaxed = true)
    private val updateDbDebtsCurrencyUseCase: UpdateDbDebtsCurrencyUseCase = mockk(relaxed = true)
    private val removeDebtorUseCase: RemoveDebtorUseCase = mockk(relaxed = true)
    private val getShareDebtorContentUseCase: GetShareDebtorContentUseCase = mockk()
    private val repository: DebtsRepository = mockk(relaxed = true)

    private val debtors = MutableStateFlow(listOf(debtor(1, "Anna", 30.0), debtor(2, "Bob", -50.0)))
    private val sortType = MutableStateFlow(SortType.NOTHING)
    private val currency = MutableStateFlow("$")

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        every { observeDebtorsListItemsUseCase.execute(TabTypes.All) } returns debtors
        every { repository.observeSortType() } returns sortType
        every { repository.observeCurrency() } returns currency
        every { repository.setSortType(any()) } answers { sortType.value = firstArg() }
        coEvery { repository.isAppFirstStart() } returns false
        coEvery { repository.getCurrency() } returns "$"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Tabs state

    @Test
    fun `state holds three tabs built from debtors, currency and sort type`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals("$", currency)
            assertEquals(SortType.NOTHING, sortType)
            assertEquals(4, tabs.getValue(TabTypes.All).items.size)
            assertEquals(listOf("Anna"), tabs.getValue(TabTypes.Debtors).names())
            assertEquals(listOf("Bob"), tabs.getValue(TabTypes.Creditors).names())
            assertEquals(-20.0, tabs.getValue(TabTypes.All).total, 0.0)
        }
    }

    @Test
    fun `state follows changes of debtors and currency`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        debtors.value = listOf(debtor(3, "Carl", 5.0))
        currency.value = "€"
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals("€", currency)
            assertEquals(listOf("Carl"), tabs.getValue(TabTypes.Debtors).names())
            assertTrue(tabs.getValue(TabTypes.Creditors).items.isEmpty())
        }
    }

    // endregion

    // region Search

    @Test
    fun `search query filters every tab`() = runTest {
        val viewModel = createViewModel()
        viewModel.onSearchOpened()
        viewModel.onSearchQueryChanged("bo")
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertTrue(isSearchActive)
            assertEquals("bo", searchQuery)
            assertEquals(listOf("Bob"), tabs.getValue(TabTypes.All).names())
            assertTrue(tabs.getValue(TabTypes.Debtors).items.isEmpty())
            assertEquals(listOf("Bob"), tabs.getValue(TabTypes.Creditors).names())
        }
    }

    @Test
    fun `closing the search clears the query`() = runTest {
        val viewModel = createViewModel()
        viewModel.onSearchOpened()
        viewModel.onSearchQueryChanged("bo")
        advanceUntilIdle()

        viewModel.onSearchClosed()
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertFalse(isSearchActive)
            assertEquals("", searchQuery)
            assertEquals(listOf("Anna", "Bob"), tabs.getValue(TabTypes.All).names())
        }
    }

    // endregion

    // region Sorting

    @Test
    fun `sort by name cycles ascending, descending, off`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val seen = (1..3).map {
            viewModel.onSortByNameClicked()
            advanceUntilIdle()
            viewModel.uiState.value.sortType
        }

        assertEquals(listOf(SortType.NAME_ASC, SortType.NAME_DESC, SortType.NOTHING), seen)
    }

    @Test
    fun `sort by amount cycles ascending, descending, off`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val seen = (1..3).map {
            viewModel.onSortByAmountClicked()
            advanceUntilIdle()
            viewModel.uiState.value.sortType
        }

        assertEquals(listOf(SortType.AMOUNT_ASC, SortType.AMOUNT_DESC, SortType.NOTHING), seen)
    }

    @Test
    fun `sort by amount starts ascending when sorted by name`() = runTest {
        sortType.value = SortType.NAME_DESC
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSortByAmountClicked()
        advanceUntilIdle()

        assertEquals(SortType.AMOUNT_ASC, viewModel.uiState.value.sortType)
    }

    // endregion

    // region First start and sync

    @Test
    fun `first start sets the locale currency and clears the flag`() = runTest {
        coEvery { repository.isAppFirstStart() } returns true

        createViewModel()
        advanceUntilIdle()

        val symbol = NumberFormat.getCurrencyInstance(Locale.getDefault()).currency.symbol
        coVerify(exactly = 1) { repository.setCurrency(symbol) }
        coVerify(exactly = 1) { updateDbDebtsCurrencyUseCase.execute() }
        coVerify(exactly = 1) { repository.setAppFirstStart(false) }
    }

    @Test
    fun `later starts keep the stored currency`() = runTest {
        createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.setCurrency(any()) }
        coVerify(exactly = 1) { updateDbDebtsCurrencyUseCase.execute() }
    }

    @Test
    fun `no sync request when every debtor has a name`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `debtor without a name requests the contacts permission for sync`() = runTest {
        debtors.value = listOf(debtor(1, "", 30.0))
        val viewModel = createViewModel()

        viewModel.events.test {
            assertEquals(HomeEvent.RequestContactsPermission(ContactsPermissionPurpose.Sync), awaitItem())
        }
    }

    @Test
    fun `granted sync permission starts the sync`() = runTest {
        val viewModel = createViewModel()

        viewModel.onContactsPermissionResult(ContactsPermissionPurpose.Sync, isGranted = true)
        advanceUntilIdle()

        coVerify(exactly = 1) { syncDebtorsWithContactsUseCase.execute() }
    }

    @Test
    fun `denied sync permission does nothing`() = runTest {
        val viewModel = createViewModel()

        viewModel.onContactsPermissionResult(ContactsPermissionPurpose.Sync, isGranted = false)
        advanceUntilIdle()

        coVerify(exactly = 0) { syncDebtorsWithContactsUseCase.execute(any()) }
    }

    // endregion

    // region Add debt

    @Test
    fun `add debt click requests the contacts permission`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAddDebtClicked()
            assertEquals(HomeEvent.RequestContactsPermission(ContactsPermissionPurpose.AddDebt), awaitItem())
        }
    }

    @Test
    fun `granted permission shows the dialog with contacts`() = runTest {
        coEvery { getContactsUseCase.execute() } returns listOf(ContactsItemModel(7, "Eve", "url"))
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onContactsPermissionResult(ContactsPermissionPurpose.AddDebt, isGranted = true)
            assertEquals(
                HomeEvent.ShowAddDebtDialog(listOf(ContactsItemViewModel(7, "Eve", "url"))),
                awaitItem(),
            )
        }
    }

    @Test
    fun `denied permission shows the dialog without contacts`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onContactsPermissionResult(ContactsPermissionPurpose.AddDebt, isGranted = false)
            assertEquals(HomeEvent.ShowAddDebtDialog(emptyList()), awaitItem())
        }
        coVerify(exactly = 0) { getContactsUseCase.execute() }
    }

    @Test
    fun `contacts loading failure still shows the dialog`() = runTest {
        coEvery { getContactsUseCase.execute() } throws IllegalStateException("no contacts")
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onContactsPermissionResult(ContactsPermissionPurpose.AddDebt, isGranted = true)
            assertEquals(HomeEvent.ShowAddDebtDialog(emptyList()), awaitItem())
        }
    }

    @Test
    fun `confirmed debt is added with the stored currency`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAddDebtConfirmed(debtData(name = "Eve", amount = -12.5))
            assertEquals(HomeEvent.DebtAdded, awaitItem())
        }
        coVerify(exactly = 1) { addDebtUseCase.execute(null, 7, "Eve", -12.5, "$", "lunch", 1_000L) }
    }

    @Test
    fun `debt with a blank name is rejected`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAddDebtConfirmed(debtData(name = " ", amount = 10.0))
            assertEquals(HomeEvent.EmptyDebtFields, awaitItem())
        }
        coVerify(exactly = 0) { addDebtUseCase.execute(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `debt with a zero amount is rejected`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAddDebtConfirmed(debtData(name = "Eve", amount = 0.0))
            assertEquals(HomeEvent.EmptyDebtFields, awaitItem())
        }
    }

    @Test
    fun `failed add sends no event`() = runTest {
        coEvery {
            addDebtUseCase.execute(any(), any(), any(), any(), any(), any(), any())
        } throws IllegalStateException("db")
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAddDebtConfirmed(debtData(name = "Eve", amount = 1.0))
            advanceUntilIdle()
            expectNoEvents()
        }
    }

    // endregion

    // region Navigation, share and removal

    @Test
    fun `debtor click opens details`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onDebtorClicked(2)
            assertEquals(HomeEvent.OpenDetails(2), awaitItem())
        }
    }

    @Test
    fun `settings click opens settings`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onSettingsClicked()
            assertEquals(HomeEvent.OpenSettings, awaitItem())
        }
    }

    @Test
    fun `share all sends the csv content`() = runTest {
        coEvery { getDebtsCsvContentUseCase.execute() } returns "csv"
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onShareAllClicked()
            assertEquals(HomeEvent.ShareAllDebts("csv"), awaitItem())
        }
    }

    @Test
    fun `share debtor sends the message built from templates`() = runTest {
        coEvery { getShareDebtorContentUseCase.execute(2, "borrowed", "lent") } returns "message"
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onShareDebtorClicked(2, "borrowed", "lent")
            assertEquals(HomeEvent.ShareDebtor("message"), awaitItem())
        }
    }

    @Test
    fun `failed share sends no event`() = runTest {
        coEvery { getDebtsCsvContentUseCase.execute() } throws IllegalStateException("io")
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onShareAllClicked()
            advanceUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun `confirmed removal removes the debtor`() = runTest {
        val viewModel = createViewModel()

        viewModel.onRemoveDebtorConfirmed(2)
        advanceUntilIdle()

        coVerify(exactly = 1) { removeDebtorUseCase.execute(2) }
    }

    // endregion

    private fun createViewModel() = HomeViewModel(
        getContactsUseCase = getContactsUseCase,
        addDebtUseCase = addDebtUseCase,
        getDebtsCsvContentUseCase = getDebtsCsvContentUseCase,
        observeDebtorsListItemsUseCase = observeDebtorsListItemsUseCase,
        syncDebtorsWithContactsUseCase = syncDebtorsWithContactsUseCase,
        updateDbDebtsCurrencyUseCase = updateDbDebtsCurrencyUseCase,
        removeDebtorUseCase = removeDebtorUseCase,
        getShareDebtorContentUseCase = getShareDebtorContentUseCase,
        repository = repository,
    )

    private fun HomeTabUiState.names() = items.filterIsInstance<HomeListItem.Debtor>().map { it.name }

    private fun debtData(name: String, amount: Double) = DebtLayoutData(
        contactId = 7,
        name = name,
        amount = amount,
        comment = "lunch",
        date = 1_000L,
        existingDebtId = null,
    )

    private fun debtor(id: Long, name: String, amount: Double) = DebtorsListItemModel.Debtor(
        id = id,
        name = name,
        amount = amount,
        currency = "$",
        lastDate = 0L,
        avatarUrl = "",
    )
}
