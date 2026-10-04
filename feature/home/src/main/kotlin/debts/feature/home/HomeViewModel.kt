@file:Suppress("TooGenericExceptionCaught")

package debts.feature.home

import androidx.lifecycle.viewModelScope
import debts.core.common.android.mvvm.BaseViewModel
import debts.core.repository.DebtsRepository
import debts.core.repository.SortType
import debts.core.usecase.AddDebtUseCase
import debts.core.usecase.GetContactsUseCase
import debts.core.usecase.GetDebtsCsvContentUseCase
import debts.core.usecase.GetShareDebtorContentUseCase
import debts.core.usecase.ObserveDebtorsListItemsUseCase
import debts.core.usecase.RemoveDebtorUseCase
import debts.core.usecase.SyncDebtorsWithContactsUseCase
import debts.core.usecase.UpdateDbDebtsCurrencyUseCase
import debts.core.usecase.data.TabTypes
import debts.feature.adddebt.DebtLayoutData
import debts.feature.contacts.adapter.ContactsItemViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.NumberFormat
import java.util.Locale

@Suppress("LongParameterList", "TooManyFunctions")
class HomeViewModel(
    private val getContactsUseCase: GetContactsUseCase,
    private val addDebtUseCase: AddDebtUseCase,
    private val getDebtsCsvContentUseCase: GetDebtsCsvContentUseCase,
    private val observeDebtorsListItemsUseCase: ObserveDebtorsListItemsUseCase,
    private val syncDebtorsWithContactsUseCase: SyncDebtorsWithContactsUseCase,
    private val updateDbDebtsCurrencyUseCase: UpdateDbDebtsCurrencyUseCase,
    private val removeDebtorUseCase: RemoveDebtorUseCase,
    private val getShareDebtorContentUseCase: GetShareDebtorContentUseCase,
    private val repository: DebtsRepository,
) : BaseViewModel<HomeUiState, HomeEvent>(HomeUiState()) {

    private val searchQuery = MutableStateFlow("")

    init {
        observeTabs()
        setUpOnFirstStart()
        requestSyncIfDebtorWithoutName()
    }

    // region Search and sorting

    fun onSearchOpened() {
        updateState { copy(isSearchActive = true) }
    }

    fun onSearchClosed() {
        searchQuery.value = ""
        updateState { copy(isSearchActive = false, searchQuery = "") }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        updateState { copy(searchQuery = query) }
    }

    fun onSortByNameClicked() {
        setSortType(
            when (uiState.value.sortType) {
                SortType.NAME_ASC -> SortType.NAME_DESC
                SortType.NAME_DESC -> SortType.NOTHING
                else -> SortType.NAME_ASC
            }
        )
    }

    fun onSortByAmountClicked() {
        setSortType(
            when (uiState.value.sortType) {
                SortType.AMOUNT_ASC -> SortType.AMOUNT_DESC
                SortType.AMOUNT_DESC -> SortType.NOTHING
                else -> SortType.AMOUNT_ASC
            }
        )
    }

    // endregion

    // region Navigation and share

    fun onSettingsClicked() {
        sendEvent(HomeEvent.OpenSettings)
    }

    fun onDebtorClicked(debtorId: Long) {
        sendEvent(HomeEvent.OpenDetails(debtorId))
    }

    fun onShareAllClicked() {
        launchLogging {
            sendEvent(HomeEvent.ShareAllDebts(getDebtsCsvContentUseCase.execute()))
        }
    }

    fun onShareDebtorClicked(debtorId: Long, borrowedTemplate: String, lentTemplate: String) {
        launchLogging {
            sendEvent(
                HomeEvent.ShareDebtor(
                    getShareDebtorContentUseCase.execute(debtorId, borrowedTemplate, lentTemplate)
                )
            )
        }
    }

    // endregion

    // region Debts

    fun onAddDebtClicked() {
        sendEvent(HomeEvent.RequestContactsPermission(ContactsPermissionPurpose.AddDebt))
    }

    fun onContactsPermissionResult(purpose: ContactsPermissionPurpose, isGranted: Boolean) {
        when (purpose) {
            ContactsPermissionPurpose.AddDebt -> showAddDebtDialog(withContacts = isGranted)
            ContactsPermissionPurpose.Sync -> if (isGranted) {
                launchLogging { syncDebtorsWithContactsUseCase.execute() }
            }
        }
    }

    fun onAddDebtConfirmed(data: DebtLayoutData) {
        if (data.name.isBlank() || data.amount == 0.0) {
            sendEvent(HomeEvent.EmptyDebtFields)
            return
        }
        launchLogging {
            addDebtUseCase.execute(
                null,
                data.contactId,
                data.name,
                data.amount,
                repository.getCurrency(),
                data.comment,
                data.date,
            )
            sendEvent(HomeEvent.DebtAdded)
        }
    }

    fun onRemoveDebtorConfirmed(debtorId: Long) {
        launchLogging { removeDebtorUseCase.execute(debtorId) }
    }

    // endregion

    private fun observeTabs() {
        viewModelScope.launch {
            combine(
                observeDebtorsListItemsUseCase.execute(TabTypes.All),
                repository.observeSortType(),
                searchQuery,
                repository.observeCurrency(),
            ) { debtors, sortType, query, currency ->
                Triple(buildHomeTabs(debtors, query, sortType), sortType, currency)
            }
                .catch { Timber.e(it) }
                .collect { (tabs, sortType, currency) ->
                    updateState { copy(tabs = tabs, sortType = sortType, currency = currency) }
                }
        }
    }

    private fun setUpOnFirstStart() {
        launchLogging {
            if (repository.isAppFirstStart()) {
                repository.setCurrency(NumberFormat.getCurrencyInstance(Locale.getDefault()).currency.symbol)
            }
            // Runs on every start, not only the first one: kept from the Rx chain this replaced.
            updateDbDebtsCurrencyUseCase.execute()
            repository.setAppFirstStart(false)
        }
    }

    private fun requestSyncIfDebtorWithoutName() {
        launchLogging {
            val debtors = observeDebtorsListItemsUseCase.execute(TabTypes.All).first()
            if (debtors.any { it.name.isEmpty() }) {
                sendEvent(HomeEvent.RequestContactsPermission(ContactsPermissionPurpose.Sync))
            }
        }
    }

    private fun showAddDebtDialog(withContacts: Boolean) {
        viewModelScope.launch {
            val contacts = if (withContacts) {
                try {
                    getContactsUseCase.execute().map { ContactsItemViewModel(it.id, it.name, it.avatarUrl) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(e)
                    emptyList()
                }
            } else {
                emptyList()
            }
            sendEvent(HomeEvent.ShowAddDebtDialog(contacts))
        }
    }

    private fun setSortType(sortType: SortType) {
        try {
            repository.setSortType(sortType)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun launchLogging(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}
