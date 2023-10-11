package debts.feature.preferences.mvi

import debts.core.common.android.mvi.MviViewModel
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
            is MainSettingsIntention.UpdateCurrencyListSelection -> MainSettingsAction.UpdateCurrencyListSelection(intent.currency)
            is MainSettingsIntention.SyncWithContacts -> MainSettingsAction.SyncWithContacts(
                intent.permission,
                intent.resultCode
            )

            MainSettingsIntention.ShowCurrencyDialog -> MainSettingsAction.ShowCurrencyDialog
            MainSettingsIntention.ShowCustomCurrencyDialog -> MainSettingsAction.ShowCustomCurrencyDialog
        }

    override val reducer: BiFunction<MainSettingsState, MainSettingsResult, MainSettingsState>
        get() = BiFunction { prevState, result ->
            when (result) {
                is MainSettingsResult.ValuesResult ->
                    prevState.copy(
                        currency = result.currency,
                        currencyListSelection = result.currencyListSelection,
                        version = result.version,
                    )

                MainSettingsResult.UpdateCurrencyStart ->
                    prevState.copy(
                        showCurrencyDialog = false,
                        showCustomCurrencyDialog = false,
                    )

                MainSettingsResult.SyncWithContactsStart ->
                    prevState.copy(
                        syncronisationState = MainSettingsState.UpdateState.START,
                    )

                MainSettingsResult.SyncWithContactsEnd ->
                    prevState.copy(
                        syncronisationState = MainSettingsState.UpdateState.END,
                    )

                MainSettingsResult.SyncWithContactsError ->
                    prevState.copy(
                        syncronisationState = MainSettingsState.UpdateState.ERROR,
                    )

                is MainSettingsResult.ShowCurrencyDialog ->
                    prevState.copy(
                        showCurrencyDialog = true,
                    )

                MainSettingsResult.ShowCustomCurrencyDialog ->
                    prevState.copy(
                        showCustomCurrencyDialog = true,
                    )

                MainSettingsResult.Error -> prevState
                MainSettingsResult.UpdateCurrencyEnd -> prevState
                MainSettingsResult.UpdateCurrencyError -> prevState
            }
        }
}
