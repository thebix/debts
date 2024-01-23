package debts.feature.preferences.mvi

import debts.core.common.android.buildconfig.BuildConfigData
import debts.core.common.android.mvi.MviInteractor
import debts.core.common.android.navigation.DebtsNavigator
import debts.core.repository.DebtsRepository
import debts.core.usecase.SyncDebtorsWithContactsUseCase
import debts.core.usecase.UpdateDbDebtsCurrencyUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MainSettingsInteractor(
    private val debtsNavigator: DebtsNavigator,
    private val updateDbDebtsCurrencyUseCase: UpdateDbDebtsCurrencyUseCase,
    private val syncDebtorsWithContactsUseCase: SyncDebtorsWithContactsUseCase,
    private val repository: DebtsRepository,
    private val buildConfigData: BuildConfigData,
) : MviInteractor<MainSettingsAction, MainSettingsResult> {

    private val initProcessor =
        ObservableTransformer<MainSettingsAction.Init, MainSettingsResult> { actions ->
            actions.switchMap { _ ->
                Observable.combineLatest<String, String, String, MainSettingsResult>(
                    repository.observeCurrency(),
                    repository.observeCurrencyListSelection(),
                    Observable.fromCallable { buildConfigData.getVersionName() }
                ) { currency, currencyListSelection, appVersion ->
                    MainSettingsResult.ValuesResult(
                        currency = currency,
                        currencyListSelection = currencyListSelection,
                        version = appVersion,
                    )
                }
                    .subscribeOn(Schedulers.io())
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(MainSettingsResult.Error)
            }
        }

    private val updateCurrencyProcessor =
        ObservableTransformer<MainSettingsAction.UpdateCurrency, MainSettingsResult> { actions ->
            actions.switchMap {
                repository.setCurrency(it.currency)
                    // to send new currency to all observers
                    .andThen(updateDbDebtsCurrencyUseCase.execute())
                    .subscribeOn(Schedulers.io())
                    .toSingleDefault(MainSettingsResult.UpdateCurrencyEnd as MainSettingsResult)
                    .doOnError { Timber.e(it) }
                    .onErrorReturnItem(MainSettingsResult.UpdateCurrencyError)
                    .toObservable()
                    .startWith(MainSettingsResult.UpdateCurrencyStart)
            }
        }

    private val updateCurrencyListSelectionProcessor =
        ObservableTransformer<MainSettingsAction.UpdateCurrencyListSelection, MainSettingsResult> { actions ->
            actions.switchMap {
                repository.setCurrencyListSelection(it.currency)
                    .subscribeOn(Schedulers.io())
                    .toSingleDefault(MainSettingsResult.UpdateCurrencyEnd as MainSettingsResult)
                    .doOnError { Timber.e(it) }
                    .onErrorReturnItem(MainSettingsResult.UpdateCurrencyError)
                    .toObservable()
                    .startWith(MainSettingsResult.UpdateCurrencyStart)
            }
        }

    private val syncWithContactsProcessor =
        ObservableTransformer<MainSettingsAction.SyncWithContacts, MainSettingsResult> { actions ->
            actions.switchMap { action ->
                debtsNavigator.isPermissionGranted(action.permission)
                    .map { isGranted -> action to isGranted }
                    .toObservable()
                    .flatMapSingle { (action, isContactsAccessGranted) ->
                        if (isContactsAccessGranted) {
                            syncDebtorsWithContactsUseCase.execute(true)
                                .toSingleDefault(MainSettingsResult.SyncWithContactsEnd as MainSettingsResult)
                        } else {
                            debtsNavigator.requestPermission(
                                action.permission,
                                action.requestCode
                            )
                                .toSingleDefault(MainSettingsResult.SyncWithContactsError)
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(MainSettingsResult.SyncWithContactsError)
                    .startWith(MainSettingsResult.SyncWithContactsStart)
            }
        }

    private val showCurrencyDialogProcessor =
        ObservableTransformer<MainSettingsAction.ShowCurrencyDialog, MainSettingsResult> { actions ->
            actions.switchMap {
                Observable.fromCallable { MainSettingsResult.ShowCurrencyDialog }
            }
        }

    private val showCustomCurrencyDialogProcessor =
        ObservableTransformer<MainSettingsAction.ShowCustomCurrencyDialog, MainSettingsResult> { actions ->
            actions.switchMap {
                Observable.fromCallable { MainSettingsResult.ShowCustomCurrencyDialog }
            }
        }

    override fun actionProcessor(): ObservableTransformer<in MainSettingsAction, out MainSettingsResult> =
        ObservableTransformer { actions ->
            actions.publish { action ->
                Observable.merge(
                    listOf(
                        action.ofType(MainSettingsAction.Init::class.java)
                            .compose(initProcessor),
                        action.ofType(MainSettingsAction.UpdateCurrency::class.java)
                            .compose(updateCurrencyProcessor),
                        action.ofType(MainSettingsAction.UpdateCurrencyListSelection::class.java)
                            .compose(updateCurrencyListSelectionProcessor),
                        action.ofType(MainSettingsAction.SyncWithContacts::class.java)
                            .compose(syncWithContactsProcessor),
                        action.ofType(MainSettingsAction.ShowCurrencyDialog::class.java)
                            .compose(showCurrencyDialogProcessor),
                        action.ofType(MainSettingsAction.ShowCustomCurrencyDialog::class.java)
                            .compose(showCustomCurrencyDialogProcessor)
                    )
                )
            }
        }
}
