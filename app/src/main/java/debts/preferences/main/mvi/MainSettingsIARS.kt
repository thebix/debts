package debts.preferences.main.mvi

import debts.common.android.mvi.*

sealed class MainSettingsIntention : MviIntention {

    object Init : MainSettingsIntention(), MviInitIntention
    data class UpdateCurrency(val currency: String) : MainSettingsIntention()
    data class SyncWithContacts(val permission: String, val resultCode: Int) : MainSettingsIntention()
}

sealed class MainSettingsAction : MviAction {

    object Init : MainSettingsAction()
    data class UpdateCurrency(val currency: String) : MainSettingsAction()
    data class SyncWithContacts(val permission: String, val requestCode: Int) : MainSettingsAction()
}

sealed class MainSettingsResult : MviResult {

    object UpdateCurrencyStart : MainSettingsResult()
    object UpdateCurrencyEnd : MainSettingsResult()
    object UpdateCurrencyError : MainSettingsResult()

    object SyncWithContactsStart : MainSettingsResult()
    object SyncWithContactsEnd : MainSettingsResult()
    object SyncWithContactsError : MainSettingsResult()

    object Error : MainSettingsResult()
}

data class MainSettingsState(
    val updateCurrencyState: OneShot<UpdateState> = OneShot.empty(),
    val syncWithContactsState: OneShot<UpdateState> = OneShot.empty(),
    val isError: OneShot<Boolean> = OneShot.empty()
) : MviState, ViewStateWithId() {

    enum class UpdateState {
        START,
        END,
        ERROR
    }
}
