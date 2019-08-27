package debts.home.list.mvi

import debts.common.android.DebtsNavigator
import debts.common.android.mvi.MviInteractor
import debts.home.list.TabTypes
import debts.repository.DebtsRepository
import debts.repository.SortType
import debts.usecase.AddDebtUseCase
import debts.usecase.DebtorsListItemModel
import debts.usecase.GetContactsUseCase
import debts.usecase.GetDebtsCsvContentUseCase
import debts.usecase.GetShareDebtorContentUseCase
import debts.usecase.ObserveDebtorsListItemsUseCase
import debts.usecase.RemoveDebtorUseCase
import debts.usecase.SyncDebtorsWithContactsUseCase
import debts.usecase.UpdateDbDebtsCurrencyUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import net.thebix.debts.R
import timber.log.Timber
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

class DebtorsInteractor(
    private val observeDebtorsListItemsUseCase: ObserveDebtorsListItemsUseCase,
    private val getContactsUseCase: GetContactsUseCase,
    private val addDebtUseCase: AddDebtUseCase,
    private val removeDebtorUseCase: RemoveDebtorUseCase,
    private val debtsNavigator: DebtsNavigator,
    private val syncDebtorsWithContactsUseCase: SyncDebtorsWithContactsUseCase,
    private val getDebtsCsvContentUseCase: GetDebtsCsvContentUseCase,
    private val getShareDebtorContentUseCase: GetShareDebtorContentUseCase,
    private val updateDbDebtsCurrencyUseCase: UpdateDbDebtsCurrencyUseCase,
    private val repository: DebtsRepository
) : MviInteractor<DebtorsAction, DebtorsResult> {

    private val initProcessor = ObservableTransformer<DebtorsAction.Init, DebtorsResult> { actions ->
        actions.switchMap { action ->
            Observable.merge(
                repository.isAppFirstStart()
                    .filter { it }
                    .flatMapCompletable {
                        repository.setCurrency(NumberFormat.getCurrencyInstance(Locale.getDefault()).currency.symbol)
                    }
                    .andThen(updateDbDebtsCurrencyUseCase.execute())
                    .andThen(repository.setAppFirstStart(false))
                    .toObservable(),
                Observable.combineLatest<List<DebtorsListItemModel.Debtor>, SortType, String, List<DebtorsListItemModel>>(
                    observeDebtorsListItemsUseCase
                        .execute(action.tabType),
                    repository.observeSortType(),
                    repository.observeDebtorsFilter(),
                    Function3 { debtors, sortType, nameFilter ->
                        getDebtorsWithAllFiltersApplied(debtors, nameFilter, sortType, action.tabType == TabTypes.All)
                    }
                )
                    .switchMap { items ->
                        Observable.fromCallable { DebtorsResult.ItemsResult(items, action.tabType) as DebtorsResult }
                    },
                checkContactsPermissionAndSyncWithContacts(action.contactPermission, action.requestCode)
                    .toObservable()
            )
                .subscribeOn(Schedulers.io())
                .doOnError { Timber.e(it) }
                .onErrorReturnItem(DebtorsResult.Error)
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
                    .doOnComplete {
                        // Tech debt: this resource id should be provided from Fragment trough intent/action
                        debtsNavigator.showToast(R.string.home_debtors_toast_debt_added)
                    }
                    .subscribeOn(Schedulers.io())
                    .toObservable<DebtorsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DebtorsResult.Error)
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
            actions.switchMap {
                syncDebtorsWithContactsUseCase.execute()
                    .subscribeOn(Schedulers.io())
                    .toObservable<DebtorsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DebtorsResult.Error)
            }
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

    // region Filtering and Sorting debtors

    private fun getDebtorsWithAllFiltersApplied(
        items: List<DebtorsListItemModel.Debtor>,
        name: String,
        sortType: SortType,
        showTitles: Boolean
    ): List<DebtorsListItemModel> {
        val filtered = getFiltered(items, name)
        @Suppress("UnnecessaryVariable")
        val filteredAndWithAbsAmountsAndSortedAndTitled: List<DebtorsListItemModel> = if (showTitles) {
            val debtors = filtered.filter { it.amount >= 0 }
            val creditors = filtered.filter { it.amount < 0 }
            return if (debtors.isNotEmpty()) {
                listOf(DebtorsListItemModel.Title(R.string.home_pager_tab_debtors))
            } else {
                emptyList()
            }
                .plus(
                    getWithAbsAmountsAndSorted(debtors, sortType)
                )
                .plus(
                    if (creditors.isNotEmpty()) {
                        listOf(DebtorsListItemModel.Title(R.string.home_pager_tab_creditors))
                    } else {
                        emptyList()
                    }
                )
                .plus(
                    getWithAbsAmountsAndSorted(creditors, sortType)
                )
        } else {
            getWithAbsAmountsAndSorted(filtered, sortType)
        }

        return filteredAndWithAbsAmountsAndSortedAndTitled
    }

    private fun getFiltered(
        items: List<DebtorsListItemModel.Debtor>,
        name: String
    ): List<DebtorsListItemModel.Debtor> =
        if (name.isNotBlank())
            items.filter {
                it.name.contains(
                    name,
                    true
                )
            }
        else
            items

    private fun getWithAbsAmountsAndSorted(
        items: List<DebtorsListItemModel.Debtor>,
        sortType: SortType
    ): List<DebtorsListItemModel.Debtor> {
        val absoluteAmounts =
            items.map { item ->
                item.copy(
                    amount = item.amount.absoluteValue
                )
            }

        return when (sortType) {
            SortType.AMOUNT_DESC -> absoluteAmounts.sortedByDescending { it.amount }
            SortType.AMOUNT_ASC -> absoluteAmounts.sortedBy { it.amount }
            SortType.NAME_DESC -> absoluteAmounts.sortedByDescending { it.name }
            SortType.NAME_ASC -> absoluteAmounts.sortedBy { it.name }
            else -> absoluteAmounts
        }
    }

    // endregion

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
