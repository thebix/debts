package debts.preferences.main.mvi

import debts.common.android.DebtsNavigator
import debts.common.android.mvi.MviInteractor
import debts.repository.DebtsRepository
import debts.usecase.SyncDebtorsWithContactsUseCase
import debts.usecase.UpdateDbDebtsCurrencyUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MainSettingsInteractor(
    private val debtsNavigator: DebtsNavigator,
    private val updateDbDebtsCurrencyUseCase: UpdateDbDebtsCurrencyUseCase,
    private val syncDebtorsWithContactsUseCase: SyncDebtorsWithContactsUseCase,
    private val repository: DebtsRepository
) : MviInteractor<MainSettingsAction, MainSettingsResult> {

    private val updateCurrencyProcessor =
        ObservableTransformer<MainSettingsAction.UpdateCurrency, MainSettingsResult> { actions ->
            actions.switchMap {
                updateDbDebtsCurrencyUseCase.execute()
                    // to send new currency to all observers
                    .andThen(repository.setCurrency(it.currency))
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

    override fun actionProcessor(): ObservableTransformer<in MainSettingsAction, out MainSettingsResult> =
        ObservableTransformer { actions ->
            actions.publish { action ->
                Observable.merge(
                    listOf(
                        action.ofType(MainSettingsAction.UpdateCurrency::class.java)
                            .compose(updateCurrencyProcessor),
                        action.ofType(MainSettingsAction.SyncWithContacts::class.java)
                            .compose(syncWithContactsProcessor)
                    )
                )
            }
        }
}
