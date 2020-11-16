package debts.home.list.mvi

import debts.common.android.DebtsNavigator
import debts.common.android.mvi.MviInteractor
import debts.home.list.TabTypes
import debts.repository.DebtsRepository
import debts.usecase.AddDebtUseCase
import debts.usecase.GetContactsUseCase
import debts.usecase.GetDebtsCsvContentUseCase
import debts.usecase.ObserveDebtorsListItemsUseCase
import debts.usecase.SyncDebtorsWithContactsUseCase
import debts.usecase.UpdateDbDebtsCurrencyUseCase
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import net.thebix.debts.R
import timber.log.Timber
import java.text.NumberFormat
import java.util.Locale

@Suppress("LongParameterList")
class HomeInteractor(
    private val getContactsUseCase: GetContactsUseCase,
    private val addDebtUseCase: AddDebtUseCase,
    private val debtsNavigator: DebtsNavigator,
    private val getDebtsCsvContentUseCase: GetDebtsCsvContentUseCase,
    private val observeDebtorsListItemsUseCase: ObserveDebtorsListItemsUseCase,
    private val syncDebtorsWithContactsUseCase: SyncDebtorsWithContactsUseCase,
    private val updateDbDebtsCurrencyUseCase: UpdateDbDebtsCurrencyUseCase,
    private val repository: DebtsRepository
) : MviInteractor<HomeAction, HomeResult> {

    private val initProcessor = ObservableTransformer<HomeAction.Init, HomeResult> { actions ->
        actions.switchMap { action ->
            Observable.merge(
                repository.isAppFirstStart()
                    .filter { it }
                    .flatMapCompletable {
                        repository.setCurrency(NumberFormat.getCurrencyInstance(Locale.getDefault()).currency.symbol)
                    }
                    .andThen(updateDbDebtsCurrencyUseCase.execute())
                    .andThen(repository.setAppFirstStart(false))
                    .toObservable<HomeResult>(),
                checkContactsPermissionAndSyncWithContacts(action.contactPermission, action.requestCode)
                    .toObservable()
            )
                .subscribeOn(Schedulers.io())
                .doOnError { Timber.e(it) }
                .onErrorReturnItem(HomeResult.Error)
        }
    }

    private val initMenuProcessor =
        ObservableTransformer<HomeAction.InitMenu, HomeResult> { actions ->
            actions.switchMap { action ->
                Observable.merge(
                    repository.observeSortType()
                        .switchMap { sortType ->
                            Observable.fromCallable { HomeResult.SortBy(sortType) as HomeResult }
                        }
                        .doOnError { error -> Timber.e(error) },
                    Completable.fromCallable { repository.setDebtorsFilter("") }
                        .toObservable()

                )
            }
        }

    private val filterProcessor =
        ObservableTransformer<HomeAction.Filter, HomeResult> { actions ->
            actions.switchMap {
                val name = it.name.toLowerCase().trim()
                Completable.fromCallable { repository.setDebtorsFilter(name) }
                    .doOnError { error -> Timber.e(error) }
                    .toObservable<HomeResult>()
            }
        }

    private val sortProcessor =
        ObservableTransformer<HomeAction.SortBy, HomeResult> { actions ->
            actions.switchMap {
                Completable.fromCallable { repository.setSortType(it.sortType) }
                    .doOnError { error -> Timber.e(error) }
                    .toMaybe<HomeResult>()
                    .onErrorReturnItem(HomeResult.Error)
                    .toObservable()
            }
        }

    private val openSettingsProcessor =
        ObservableTransformer<HomeAction.OpenSettings, HomeResult> { actions ->
            actions.switchMap {
                debtsNavigator.openSettings()
                    .subscribeOn(Schedulers.io())
                    .toObservable<HomeResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(HomeResult.Error)
            }
        }

    private val shareAllDebtsProcessor =
        ObservableTransformer<HomeAction.ShareAllDebts, HomeResult> { actions ->
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
                    .toObservable<HomeResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(HomeResult.Error)
            }
        }

    private fun checkContactsPermissionAndSyncWithContacts(contactPermission: String, requestCode: Int) =
        observeDebtorsListItemsUseCase.execute(TabTypes.All)
            .take(1)
            .singleElement()
            .filter { items -> items.any { it.name.isEmpty() } }
            .flatMap {
                debtsNavigator.isPermissionGranted(contactPermission)
                    .toMaybe()
            }
            .flatMapCompletable { isContactsAccessGranted ->
                if (isContactsAccessGranted) {
                    syncDebtorsWithContactsUseCase.execute()
                } else {
                    debtsNavigator.requestPermission(
                        contactPermission,
                        requestCode
                    )
                }
            }

    private val openAddDebtDialogProcessor =
        ObservableTransformer<HomeAction.OpenAddDebtDialog, HomeResult> { actions ->
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
                            .toObservable<HomeResult>()
                    }
                }
                .subscribeOn(Schedulers.io())
                .doOnError { Timber.e(it) }
                .onErrorReturnItem(HomeResult.ShowAddDebtDialog(emptyList()))
        }

    private fun getContactsResult() = getContactsUseCase
        .execute()
        .flatMap { items ->
            Single.fromCallable { HomeResult.ShowAddDebtDialog(items) as HomeResult }
        }
        .toObservable()

    private val addDebtProcessor =
        ObservableTransformer<HomeAction.AddDebt, HomeResult> { actions ->
            actions.switchMap {
                repository.getCurrency()
                    .flatMapCompletable { currency ->
                        addDebtUseCase
                            .execute(null, it.contactId, it.name, it.amount, currency, it.comment, it.date)
                    }
                    .doOnComplete {
                        // TODO: this resource id should be provided from Fragment through intent/action
                        debtsNavigator.showToast(R.string.home_debtors_toast_debt_added)
                    }
                    .subscribeOn(Schedulers.io())
                    .toObservable<HomeResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(HomeResult.Error)
            }
        }

    override fun actionProcessor(): ObservableTransformer<in HomeAction, out HomeResult> =
        ObservableTransformer { actions ->
            actions.publish { action ->
                Observable.merge(
                    listOf(
                        action.ofType(HomeAction.Init::class.java)
                            .compose(initProcessor),
                        action.ofType(HomeAction.InitMenu::class.java)
                            .compose(initMenuProcessor),
                        action.ofType(HomeAction.OpenAddDebtDialog::class.java)
                            .compose(openAddDebtDialogProcessor),
                        action.ofType(HomeAction.AddDebt::class.java)
                            .compose(addDebtProcessor),
                        action.ofType(HomeAction.Filter::class.java)
                            .compose(filterProcessor),
                        action.ofType(HomeAction.SortBy::class.java)
                            .compose(sortProcessor),
                        action.ofType(HomeAction.OpenSettings::class.java)
                            .compose(openSettingsProcessor),
                        action.ofType(HomeAction.ShareAllDebts::class.java)
                            .compose(shareAllDebtsProcessor)
                    )
                )
            }
        }
}
