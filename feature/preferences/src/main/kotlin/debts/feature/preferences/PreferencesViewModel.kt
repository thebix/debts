@file:Suppress("TooGenericExceptionCaught")

package debts.feature.preferences

import android.Manifest
import androidx.lifecycle.viewModelScope
import debts.core.common.android.buildconfig.BuildConfigData
import debts.core.common.android.mvvm.BaseViewModel
import debts.core.repository.DebtsRepository
import debts.core.usecase.SyncDebtorsWithContactsUseCase
import debts.core.usecase.UpdateDbDebtsCurrencyUseCase
import debts.feature.preferences.PreferencesUiState.SyncStatus
import kotlinx.coroutines.launch
import timber.log.Timber

class PreferencesViewModel(
    private val updateDbDebtsCurrencyUseCase: UpdateDbDebtsCurrencyUseCase,
    private val syncDebtorsWithContactsUseCase: SyncDebtorsWithContactsUseCase,
    private val repository: DebtsRepository,
    private val buildConfigData: BuildConfigData,
) : BaseViewModel<PreferencesUiState, PreferencesEvent>(PreferencesUiState()) {

    fun init() {
        viewModelScope.launch {
            val stored = repository.getCurrency()
            val isCustom = stored.isNotEmpty() && stored !in PreferencesUiState.STANDARD_CURRENCIES
            updateState {
                copy(
                    selectedCurrency = if (isCustom) PreferencesUiState.CUSTOM_KEY else stored,
                    isCustomCurrency = isCustom,
                    customCurrencyText = if (isCustom) stored else "",
                    versionName = buildConfigData.getVersionName(),
                )
            }
        }
    }

    fun onCurrencySelected(option: String) {
        if (option == PreferencesUiState.CUSTOM_KEY) {
            updateState { copy(selectedCurrency = option, isCustomCurrency = true) }
            return
        }
        viewModelScope.launch {
            try {
                repository.setCurrency(option)
                updateDbDebtsCurrencyUseCase.execute()
                updateState { copy(selectedCurrency = option, isCustomCurrency = false, customCurrencyText = "") }
            } catch (e: Exception) {
                Timber.e(e)
                sendEvent(PreferencesEvent.ShowError)
            }
        }
    }

    fun onCustomCurrencyChanged(text: String) {
        updateState { copy(customCurrencyText = text) }
    }

    fun onCustomCurrencySubmitted(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                repository.setCurrency(text)
                updateDbDebtsCurrencyUseCase.execute()
            } catch (e: Exception) {
                Timber.e(e)
                sendEvent(PreferencesEvent.ShowError)
            }
        }
    }

    fun onSyncWithContactsClicked() {
        sendEvent(
            PreferencesEvent.RequestPermission(
                permission = Manifest.permission.READ_CONTACTS,
                requestCode = READ_CONTACTS_PERMISSION_CODE,
            )
        )
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            updateState { copy(syncStatus = SyncStatus.Updating) }
            try {
                syncDebtorsWithContactsUseCase.execute(true)
                updateState { copy(syncStatus = SyncStatus.Updated) }
            } catch (e: Exception) {
                Timber.e(e)
                updateState { copy(syncStatus = SyncStatus.Error) }
            }
        }
    }

    companion object {
        const val READ_CONTACTS_PERMISSION_CODE = 3
    }
}
