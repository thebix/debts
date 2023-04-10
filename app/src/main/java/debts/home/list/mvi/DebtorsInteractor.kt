package debts.home.list.mvi

import debts.common.android.DebtsNavigator
import debts.common.android.mvi.MviInteractor
import debts.home.list.TabTypes
import debts.repository.DebtsRepository
import debts.repository.SortType
import debts.usecase.DebtorsListItemModel
import debts.usecase.GetShareDebtorContentUseCase
import debts.usecase.ObserveDebtorsListItemsUseCase
import debts.usecase.RemoveDebtorUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import net.thebix.debts.R
import timber.log.Timber
import kotlin.math.absoluteValue

class DebtorsInteractor(
    private val observeDebtorsListItemsUseCase: ObserveDebtorsListItemsUseCase,
    private val removeDebtorUseCase: RemoveDebtorUseCase,
    private val debtsNavigator: DebtsNavigator,
    private val getShareDebtorContentUseCase: GetShareDebtorContentUseCase,
    private val repository: DebtsRepository,
) : MviInteractor<DebtorsAction, DebtorsResult> {

    private val initProcessor =
        ObservableTransformer<DebtorsAction.Init, DebtorsResult> { actions ->
            actions.switchMap { action ->
                Observable.combineLatest<List<DebtorsListItemModel.Debtor>, SortType, String, String, Pair<Pair<String, Double>, List<DebtorsListItemModel>>>(
                    observeDebtorsListItemsUseCase
                        .execute(action.tabType),
                    repository.observeSortType(),
                    repository.observeDebtorsFilter(),
                    repository.observeCurrency(),
                    Function4 { debtors, sortType, nameFilter, defaultCurrency ->
                        val filtered = getFiltered(debtors, nameFilter)
                        val totalAmount = filtered.sumByDouble { it.amount }
                        defaultCurrency to totalAmount to getDebtorsWithAbsAmountsAndSortingApplied(
                            filtered,
                            sortType,
                            action.tabType == TabTypes.All
                        )
                    }
                )
                    .switchMap { (defaultCurrencyAndTotalAmount, items) ->
                        val (defaultCurrency, totalAmount) = defaultCurrencyAndTotalAmount
                        Observable.fromCallable {
                            DebtorsResult.ItemsResult(
                                items,
                                totalAmount,
                                defaultCurrency,
                                action.tabType
                            ) as DebtorsResult
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .doOnError { Timber.e(it) }
                    .onErrorReturnItem(DebtorsResult.Error)
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

    private val shareDebtorProcessor =
        ObservableTransformer<DebtorsAction.ShareDebtor, DebtorsResult> { actions ->
            actions.switchMap { action ->
                getShareDebtorContentUseCase.execute(
                    action.debtorId,
                    action.borrowedTemplate,
                    action.lentTemplate
                )
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

    private fun getFiltered(
        items: List<DebtorsListItemModel.Debtor>,
        name: String,
    ): List<DebtorsListItemModel.Debtor> =
        if (name.isNotBlank()) {
            items.filter {
                it.name.contains(
                    name,
                    true
                )
            }
        } else {
            items
        }

    private fun getDebtorsWithAbsAmountsAndSortingApplied(
        items: List<DebtorsListItemModel.Debtor>,
        sortType: SortType,
        showTitles: Boolean,
    ): List<DebtorsListItemModel> {
        return if (showTitles) {
            val debtors = items.filter { it.amount >= 0 }
            val creditors = items.filter { it.amount < 0 }
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
            getWithAbsAmountsAndSorted(items, sortType)
        }
    }

    private fun getWithAbsAmountsAndSorted(
        items: List<DebtorsListItemModel.Debtor>,
        sortType: SortType,
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
                        action.ofType(DebtorsAction.RemoveDebtor::class.java)
                            .compose(removeDebtorProcessor),
                        action.ofType(DebtorsAction.OpenDetails::class.java)
                            .compose(openDetailsProcessor),
                        action.ofType(DebtorsAction.ShareDebtor::class.java)
                            .compose(shareDebtorProcessor)
                    )
                )
            }
        }
}
