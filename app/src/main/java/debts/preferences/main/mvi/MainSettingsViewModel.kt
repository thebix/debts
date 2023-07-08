package debts.preferences.main.mvi

import debts.core.common.android.mvi.MviViewModel
import debts.core.common.android.mvi.OneShot
import io.reactivex.functions.BiFunction

class MainSettingsViewModel(
    interactor: MainSettingsInteractor,
) : MviViewModel<MainSettingsIntention, MainSettingsAction, MainSettingsResult, MainSettingsState>(interactor) {

    override val defaultState: MainSettingsState
        get() = MainSettingsState()

    override fun actionFromIntention(intent: MainSettingsIntention): MainSettingsAction =
        when (intent) {
            is MainSettingsIntention.Init -> MainSettingsAction.Init
            is MainSettingsIntention.UpdateCurrency -> MainSettingsAction.UpdateCurrency(intent.currency)
            is MainSettingsIntention.SyncWithContacts -> MainSettingsAction.SyncWithContacts(
                intent.permission,
                intent.resultCode
            )
        }

    override val reducer: BiFunction<MainSettingsState, MainSettingsResult, MainSettingsState>
        get() = BiFunction { prevState, result ->
            when (result) {
                MainSettingsResult.Error ->
                    prevState.copy(isError = OneShot(true))

                MainSettingsResult.UpdateCurrencyStart ->
                    prevState.copy(updateCurrencyState = OneShot(MainSettingsState.UpdateState.START))

                MainSettingsResult.UpdateCurrencyEnd ->
                    prevState.copy(updateCurrencyState = OneShot(MainSettingsState.UpdateState.END))

                MainSettingsResult.UpdateCurrencyError ->
                    prevState.copy(updateCurrencyState = OneShot(MainSettingsState.UpdateState.ERROR))

                MainSettingsResult.SyncWithContactsStart ->
                    prevState.copy(syncWithContactsState = OneShot(MainSettingsState.UpdateState.START))

                MainSettingsResult.SyncWithContactsEnd ->
                    prevState.copy(syncWithContactsState = OneShot(MainSettingsState.UpdateState.END))

                MainSettingsResult.SyncWithContactsError ->
                    prevState.copy(syncWithContactsState = OneShot(MainSettingsState.UpdateState.ERROR))
            }
        }
}
