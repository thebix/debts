package debts.home.list.mvi

import debts.common.android.DebtsNavigator
import debts.common.android.mvi.MviInteractor
import debts.repository.DebtsRepository
import debts.usecase.*
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class DebtorsInteractor(
    private val observeDebtorsListItemsUseCase: ObserveDebtorsListItemsUseCase,
    private val getContactsUseCase: GetContactsUseCase,
    private val addDebtUseCase: AddDebtUseCase,
    private val removeDebtorUseCase: RemoveDebtorUseCase,
    private val debtsNavigator: DebtsNavigator,
    private val syncDebtorsWithContactsUseCase: SyncDebtorsWithContactsUseCase,
    private val getDebtsCsvContentUseCase: GetDebtsCsvContentUseCase,
    private val getShareDebtorContentUseCase: GetShareDebtorContentUseCase,
    private val repository: DebtsRepository
) : MviInteractor<DebtorsAction, DebtorsResult> {

    private val initProcessor = ObservableTransformer<DebtorsAction, DebtorsResult> { actions ->
        actions.switchMap {
            observeDebtorsListItemsUseCase
                .execute()
                .subscribeOn(Schedulers.io())
                .switchMap { items ->
                    Observable.fromCallable { DebtorsResult.ItemsResult(items) as DebtorsResult }
                }
                .doOnError { Timber.e(it) }
                .onErrorReturnItem(DebtorsResult.Error)
        }
    }

    private val openAddDebtDialogProcessor =
        ObservableTransformer<DebtorsAction.OpenAddDebtDialog, DebtorsResult> { actions ->
            actions.switchMap { action ->
                debtsNavigator.isPermissionGranted(action.contactPermission)
                    .map { isGranted -> action to isGranted }
                    .toObservable()
            }
                .subscribeOn(Schedulers.io())
                .flatMap { (action, isContactsAccessGranted) ->
                    if (isContactsAccessGranted) {
                        getContactsResult()
                    } else {
                        debtsNavigator.requestPermission(
                            action.contactPermission,
                            action.requestCode
                        )
                            .toObservable<DebtorsResult>()
                    }
                }
                .subscribeOn(Schedulers.io())
                .doOnError { Timber.e(it) }
                .onErrorReturnItem(DebtorsResult.ShowAddDebtDialog(emptyList()))
        }

    private fun getContactsResult() = getContactsUseCase
        .execute()
        .flatMap { items ->
            Single.fromCallable { DebtorsResult.ShowAddDebtDialog(items) as DebtorsResult }
        }
        .toObservable()

    private val addDebtProcessor =
        ObservableTransformer<DebtorsAction.AddDebt, DebtorsResult> { actions ->
            actions.switchMap {
                repository.getCurrency()
                    .flatMapCompletable { currency ->
                        addDebtUseCase
                            .execute(null, it.contactId, it.name, it.amount, currency, it.comment)
                    }
                    .subscribeOn(Schedulers.io())
                    .toObservable<DebtorsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DebtorsResult.Error)
            }
        }

    private val filterProcessor =
        ObservableTransformer<DebtorsAction.Filter, DebtorsResult> { actions ->
            actions.switchMap {
                Observable.fromCallable { DebtorsResult.Filter(it.name.toLowerCase().trim()) }
            }
        }

    private val sortProcessor =
        ObservableTransformer<DebtorsAction.SortBy, DebtorsResult> { actions ->
            actions.switchMap {
                Observable.fromCallable { DebtorsResult.SortBy(it.sortType) }
            }
        }

    private val removeDebtorProcessor =
        ObservableTransformer<DebtorsAction.RemoveDebtor, DebtorsResult> { actions ->
            actions.switchMap {
                removeDebtorUseCase
                    .execute(it.debtorId)
                    .subscribeOn(Schedulers.io())
                    .toObservable<DebtorsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DebtorsResult.Error)
            }
        }

    private val openDetailsProcessor =
        ObservableTransformer<DebtorsAction.OpenDetails, DebtorsResult> { actions ->
            actions.switchMap { action ->
                debtsNavigator.openDetails(action.debtorId)
                    .subscribeOn(Schedulers.io())
                    .toObservable<DebtorsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DebtorsResult.Error)
            }
        }

    private val syncWithContactsProcessor =
        ObservableTransformer<DebtorsAction.SyncWithContacts, DebtorsResult> { actions ->
            actions.switchMap { action ->
                debtsNavigator.isPermissionGranted(action.contactPermission)
                    .map { isGranted -> action to isGranted }
                    .toObservable()
            }.flatMapCompletable { (action, isContactsAccessGranted) ->
                if (isContactsAccessGranted) {
                    syncDebtorsWithContactsUseCase
                        .execute()
                } else {
                    debtsNavigator.requestPermission(
                        action.contactPermission,
                        action.requestCode
                    )
                }
            }
                .subscribeOn(Schedulers.io())
                .toObservable<DebtorsResult>()
                .doOnError { error -> Timber.e(error) }
                .onErrorReturnItem(DebtorsResult.Error)
        }

    private val openSettingsProcessor =
        ObservableTransformer<DebtorsAction.OpenSettings, DebtorsResult> { actions ->
            actions.switchMap {
                debtsNavigator.openSettings()
                    .subscribeOn(Schedulers.io())
                    .toObservable<DebtorsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DebtorsResult.Error)
            }
        }

    private val shareAllDebtsProcessor =
        ObservableTransformer<DebtorsAction.ShareAllDebts, DebtorsResult> { actions ->
            actions.switchMap { action ->
                getDebtsCsvContentUseCase.execute()
                    .flatMapCompletable { content ->
                        debtsNavigator.sendExplicitFile(
                            action.titleText,
                            "debts.csv",
                            content,
                            "text/csv"
                        )
                    }
                    .subscribeOn(Schedulers.io())
                    .toObservable<DebtorsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DebtorsResult.Error)
            }
        }

    private val shareDebtorProcessor =
        ObservableTransformer<DebtorsAction.ShareDebtor, DebtorsResult> { actions ->
            actions.switchMap { action ->
                getShareDebtorContentUseCase.execute(action.debtorId, action.borrowedTemplate, action.lentTemplate)
                    .flatMapCompletable { content ->
                        debtsNavigator.sendExplicit(
                            action.titleText,
                            content
                        )
                    }
                    .subscribeOn(Schedulers.io())
                    .toObservable<DebtorsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DebtorsResult.Error)
            }
        }

    override fun actionProcessor(): ObservableTransformer<in DebtorsAction, out DebtorsResult> =
        ObservableTransformer { actions ->
            actions.publish { action ->
                Observable.merge(
                    listOf(
                        action.ofType(DebtorsAction.Init::class.java)
                            .compose(initProcessor),
                        action.ofType(DebtorsAction.OpenAddDebtDialog::class.java)
                            .compose(openAddDebtDialogProcessor),
                        action.ofType(DebtorsAction.AddDebt::class.java)
                            .compose(addDebtProcessor),
                        action.ofType(DebtorsAction.Filter::class.java)
                            .compose(filterProcessor),
                        action.ofType(DebtorsAction.SortBy::class.java)
                            .compose(sortProcessor),
                        action.ofType(DebtorsAction.RemoveDebtor::class.java)
                            .compose(removeDebtorProcessor),
                        action.ofType(DebtorsAction.OpenDetails::class.java)
                            .compose(openDetailsProcessor),
                        action.ofType(DebtorsAction.SyncWithContacts::class.java)
                            .compose(syncWithContactsProcessor),
                        action.ofType(DebtorsAction.OpenSettings::class.java)
                            .compose(openSettingsProcessor),
                        action.ofType(DebtorsAction.ShareAllDebts::class.java)
                            .compose(shareAllDebtsProcessor),
                        action.ofType(DebtorsAction.ShareDebtor::class.java)
                            .compose(shareDebtorProcessor)
                    )
                )
            }
        }
}
