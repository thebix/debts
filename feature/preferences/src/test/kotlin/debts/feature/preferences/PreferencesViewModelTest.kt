package debts.feature.preferences

import android.Manifest
import app.cash.turbine.test
import debts.core.common.android.buildconfig.BuildConfigData
import debts.core.repository.DebtsRepository
import debts.core.usecase.SyncDebtorsWithContactsUseCase
import debts.core.usecase.UpdateDbDebtsCurrencyUseCase
import debts.feature.preferences.PreferencesUiState.SyncStatus
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
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
import kotlin.test.assertIs

class PreferencesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: DebtsRepository = mockk()
    private val buildConfigData: BuildConfigData = mockk()
    private val updateDbDebtsCurrencyUseCase: UpdateDbDebtsCurrencyUseCase = mockk()
    private val syncDebtorsWithContactsUseCase: SyncDebtorsWithContactsUseCase = mockk()

    private lateinit var viewModel: PreferencesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PreferencesViewModel(
            updateDbDebtsCurrencyUseCase = updateDbDebtsCurrencyUseCase,
            syncDebtorsWithContactsUseCase = syncDebtorsWithContactsUseCase,
            repository = repository,
            buildConfigData = buildConfigData,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads standard currency and version`() = runTest {
        coEvery { repository.getCurrency() } returns "$"
        every { buildConfigData.getVersionName() } returns "1.0.0"

        viewModel.init()
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals("$", selectedCurrency)
            assertFalse(isCustomCurrency)
            assertEquals("", customCurrencyText)
            assertEquals("1.0.0", versionName)
        }
    }

    @Test
    fun `init with unknown currency sets isCustomCurrency true`() = runTest {
        coEvery { repository.getCurrency() } returns "BTC"
        every { buildConfigData.getVersionName() } returns "1.0.0"

        viewModel.init()
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals(PreferencesUiState.CUSTOM_KEY, selectedCurrency)
            assertTrue(isCustomCurrency)
            assertEquals("BTC", customCurrencyText)
        }
    }

    @Test
    fun `init with empty currency keeps default state`() = runTest {
        coEvery { repository.getCurrency() } returns ""
        every { buildConfigData.getVersionName() } returns "2.0.0"

        viewModel.init()
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals("", selectedCurrency)
            assertFalse(isCustomCurrency)
        }
    }

    @Test
    fun `onCurrencySelected with standard currency calls use cases and updates state`() = runTest {
        coJustRun { updateDbDebtsCurrencyUseCase.execute() }
        coJustRun { repository.setCurrency("€") }

        viewModel.onCurrencySelected("€")
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertEquals("€", selectedCurrency)
            assertFalse(isCustomCurrency)
            assertEquals("", customCurrencyText)
        }
        coVerify { updateDbDebtsCurrencyUseCase.execute() }
        coVerify { repository.setCurrency("€") }
    }

    @Test
    fun `onCurrencySelected with Custom key does not save to repository`() = runTest {
        viewModel.onCurrencySelected(PreferencesUiState.CUSTOM_KEY)

        with(viewModel.uiState.value) {
            assertEquals(PreferencesUiState.CUSTOM_KEY, selectedCurrency)
            assertTrue(isCustomCurrency)
        }
        coVerify(exactly = 0) { repository.setCurrency(any()) }
    }

    @Test
    fun `onCustomCurrencyChanged updates state without saving`() = runTest {
        viewModel.onCustomCurrencyChanged("XYZ")

        assertEquals("XYZ", viewModel.uiState.value.customCurrencyText)
        coVerify(exactly = 0) { repository.setCurrency(any()) }
    }

    @Test
    fun `onCustomCurrencySubmitted saves to repository`() = runTest {
        coJustRun { updateDbDebtsCurrencyUseCase.execute() }
        coJustRun { repository.setCurrency("BTC") }

        viewModel.onCustomCurrencySubmitted("BTC")
        advanceUntilIdle()

        coVerify { updateDbDebtsCurrencyUseCase.execute() }
        coVerify { repository.setCurrency("BTC") }
    }

    @Test
    fun `onCustomCurrencySubmitted ignores blank text`() = runTest {
        viewModel.onCustomCurrencySubmitted("   ")
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.setCurrency(any()) }
    }

    @Test
    fun `onSyncWithContactsClicked sends RequestPermission event`() = runTest {
        viewModel.events.test {
            viewModel.onSyncWithContactsClicked()
            advanceUntilIdle()
            val event = awaitItem()
            assertIs<PreferencesEvent.RequestPermission>(event)
            assertEquals(Manifest.permission.READ_CONTACTS, event.permission)
            assertEquals(PreferencesViewModel.READ_CONTACTS_PERMISSION_CODE, event.requestCode)
        }
    }

    @Test
    fun `onPermissionGranted syncs contacts and sets Updated status`() = runTest {
        coJustRun { syncDebtorsWithContactsUseCase.execute(true) }

        viewModel.onPermissionGranted()
        advanceUntilIdle()

        assertEquals(SyncStatus.Updated, viewModel.uiState.value.syncStatus)
    }

    @Test
    fun `onPermissionGranted on sync failure sets Error status`() = runTest {
        coEvery { syncDebtorsWithContactsUseCase.execute(true) } throws RuntimeException("network error")

        viewModel.onPermissionGranted()
        advanceUntilIdle()

        assertEquals(SyncStatus.Error, viewModel.uiState.value.syncStatus)
    }
}
