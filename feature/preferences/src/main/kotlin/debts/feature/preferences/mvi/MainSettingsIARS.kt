package debts.feature.preferences.mvi

import debts.core.common.android.mvi.MviAction
import debts.core.common.android.mvi.MviInitIntention
import debts.core.common.android.mvi.MviIntention
import debts.core.common.android.mvi.MviResult
import debts.core.common.android.mvi.MviState
import debts.core.common.android.mvi.ViewStateWithId

sealed class MainSettingsIntention : MviIntention {

    object Init : MainSettingsIntention(), MviInitIntention
    data class UpdateCurrency(val currency: String) : MainSettingsIntention()
    data class UpdateCurrencyListSelection(val currency: String) : MainSettingsIntention()
    data class SyncWithContacts(val permission: String, val resultCode: Int) :
        MainSettingsIntention()

    data object ShowCurrencyDialog : MainSettingsIntention()
    data object ShowCustomCurrencyDialog : MainSettingsIntention()
}

sealed class MainSettingsAction : MviAction {

    object Init : MainSettingsAction()
    data class UpdateCurrency(val currency: String) : MainSettingsAction()
    data class UpdateCurrencyListSelection(val currency: String) : MainSettingsAction()
    data class SyncWithContacts(val permission: String, val requestCode: Int) : MainSettingsAction()

    data object ShowCurrencyDialog : MainSettingsAction()
    data object ShowCustomCurrencyDialog : MainSettingsAction()
}

sealed class MainSettingsResult : MviResult {

    data class ValuesResult(
        val currency: String = "",
        val currencyListSelection: String = "",
        val version: String = "",
    ) : MainSettingsResult()

    object UpdateCurrencyStart : MainSettingsResult()
    object UpdateCurrencyEnd : MainSettingsResult()
    object UpdateCurrencyError : MainSettingsResult()

    object SyncWithContactsStart : MainSettingsResult()
    object SyncWithContactsEnd : MainSettingsResult()
    object SyncWithContactsError : MainSettingsResult()

    object Error : MainSettingsResult()

    data object ShowCurrencyDialog : MainSettingsResult()
    data object ShowCustomCurrencyDialog : MainSettingsResult()
}

data class MainSettingsState(
    val currency: String = "",
    val currencyListSelection: String = "",
    val version: String = "",
    val syncronisationState: UpdateState = UpdateState.IDLE,
    val showCurrencyDialog: Boolean = false,
    val showCustomCurrencyDialog: Boolean = false,
) : MviState, ViewStateWithId() {

    enum class UpdateState {
        IDLE,
        START,
        END,
        ERROR
    }

    data class SettingsCurrency(
        val title: String,
        val value: String,
    )
}
