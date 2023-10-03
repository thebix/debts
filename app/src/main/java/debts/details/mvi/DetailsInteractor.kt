package debts.details.mvi

import debts.core.common.android.mvi.MviInteractor
import debts.core.common.android.navigation.DebtsNavigator
import debts.core.repository.DebtsRepository
import debts.core.usecase.AddDebtUseCase
import debts.core.usecase.ClearHistoryUseCase
import debts.core.usecase.GetDebtUseCase
import debts.core.usecase.GetShareDebtorContentUseCase
import debts.core.usecase.ObserveDebtorUseCase
import debts.core.usecase.ObserveDebtsUseCase
import debts.core.usecase.RemoveDebtUseCase
import debts.core.usecase.RemoveDebtorUseCase
import debts.core.usecase.UpdateDebtUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import net.thebix.debts.R
import timber.log.Timber

@Suppress("LongParameterList")
class DetailsInteractor(
    private val debtsNavigator: DebtsNavigator,
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val addDebtUseCase: AddDebtUseCase,
    private val observeDebtorUseCase: ObserveDebtorUseCase,
    private val observeDebtsUseCase: ObserveDebtsUseCase,
    private val removeDebtUseCase: RemoveDebtUseCase,
    private val getDebtUseCase: GetDebtUseCase,
    private val updateDebtUseCase: UpdateDebtUseCase,
    private val removeDebtorUseCase: RemoveDebtorUseCase,
    private val getShareDebtorContentUseCase: GetShareDebtorContentUseCase,
    private val repository: DebtsRepository,

    ) : MviInteractor<DetailsAction, DetailsResult> {

    private val initProcessor =
        ObservableTransformer<DetailsAction.Init, DetailsResult> { actions ->
            actions.switchMap { action ->
                Observable.merge(
                    observeDebtorUseCase.execute(action.id)
                        .map {
                            DetailsResult.Debtor(it.name, it.amount, it.currency, it.avatarUrl)
                        },
                    observeDebtsUseCase.execute(action.id)
                        .map { items ->
                            DetailsResult.History(items.map { it })
                        }
                )
                    .subscribeOn(Schedulers.io())
                    .doOnError { Timber.e(it) }
                    .onErrorReturnItem(DetailsResult.Error)
            }
        }

    private val clearHistoryProcessor =
        ObservableTransformer<DetailsAction.ClearHistory, DetailsResult> { actions ->
            actions.switchMap { action ->
                clearHistoryUseCase
                    .execute(action.id)
                    .subscribeOn(Schedulers.io())
                    .doOnError { Timber.e(it) }
                    .toSingleDefault(DetailsResult.History(emptyList()) as DetailsResult)
                    .onErrorReturnItem(DetailsResult.Error)
                    .toObservable()
            }
        }

    private val addDebtProcessor =
        ObservableTransformer<DetailsAction.AddDebt, DetailsResult> { actions ->
            actions.switchMap { action ->
                repository.getCurrency()
                    .flatMapCompletable { currency ->
                        addDebtUseCase
                            .execute(
                                action.debtorId,
                                null,
                                "",
                                action.amount,
                                currency,
                                action.comment,
                                action.date
                            )
                    }
                    .doOnComplete {
                        // TODO: this resource id should be provided from Fragment through intent/action
                        debtsNavigator.showToast(R.string.home_debtors_toast_debt_added)
                    }
                    .subscribeOn(Schedulers.io())
                    .toObservable<DetailsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DetailsResult.Error)
            }
        }

    private val removeDebtProcessor =
        ObservableTransformer<DetailsAction.RemoveDebt, DetailsResult> { actions ->
            actions.switchMap {
                removeDebtUseCase
                    .execute(it.id)
                    .subscribeOn(Schedulers.io())
                    .toObservable<DetailsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DetailsResult.Error)
            }
        }

    private val editDebtProcessor =
        ObservableTransformer<DetailsAction.EditDebt, DetailsResult> { actions ->
            actions.switchMap {
                getDebtUseCase
                    .execute(it.id)
                    .subscribeOn(Schedulers.io())
                    .map { debtModel ->
                        DetailsResult.EditDebt(
                            debtId = it.id,
                            amount = debtModel.amount,
                            comment = debtModel.comment,
                            date = debtModel.date
                        ) as DetailsResult
                    }
                    .toObservable()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DetailsResult.Error)
            }
        }

    private val editDebtSaveProcessor =
        ObservableTransformer<DetailsAction.EditDebtSave, DetailsResult> { actions ->
            actions.switchMap {
                getDebtUseCase.execute(it.debtId)
                    .subscribeOn(Schedulers.io())
                    .flatMapCompletable { debtModel ->
                        updateDebtUseCase
                            .execute(
                                id = debtModel.id,
                                debtorId = debtModel.debtorId,
                                amount = it.amount,
                                date = it.date,
                                currency = debtModel.currency,
                                comment = it.comment
                            )
                            .doOnError { error -> Timber.e(error) }
                    }
                    .doOnComplete {
                        // TODO: this resource id should be provided from Fragment through intent/action
                        debtsNavigator.showToast(R.string.home_debtors_toast_debt_changed)
                    }
                    .toObservable<DetailsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DetailsResult.Error)
            }
        }

    private val removeDebtorProcessor =
        ObservableTransformer<DetailsAction.RemoveDebtor, DetailsResult> { actions ->
            actions.switchMap {
                removeDebtorUseCase
                    .execute(it.debtorId)
                    .subscribeOn(Schedulers.io())
                    .toSingleDefault(DetailsResult.DebtorRemoved as DetailsResult)
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DetailsResult.Error)
                    .toObservable()
            }
        }

    private val shareDebtorProcessor =
        ObservableTransformer<DetailsAction.ShareDebtor, DetailsResult> { actions ->
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
                    .toObservable<DetailsResult>()
                    .doOnError { error -> Timber.e(error) }
                    .onErrorReturnItem(DetailsResult.Error)
            }
        }

    override fun actionProcessor(): ObservableTransformer<in DetailsAction, out DetailsResult> =
        ObservableTransformer { actions ->
            actions.publish { action ->
                Observable.merge(
                    listOf(
                        action.ofType(DetailsAction.Init::class.java)
                            .compose(initProcessor),
                        action.ofType(DetailsAction.ClearHistory::class.java)
                            .compose(clearHistoryProcessor),
                        action.ofType(DetailsAction.AddDebt::class.java)
                            .compose(addDebtProcessor),
                        action.ofType(DetailsAction.RemoveDebt::class.java)
                            .compose(removeDebtProcessor),
                        action.ofType(DetailsAction.EditDebt::class.java)
                            .compose(editDebtProcessor),
                        action.ofType(DetailsAction.EditDebtSave::class.java)
                            .compose(editDebtSaveProcessor),
                        action.ofType(DetailsAction.RemoveDebtor::class.java)
                            .compose(removeDebtorProcessor),
                        action.ofType(DetailsAction.ShareDebtor::class.java)
                            .compose(shareDebtorProcessor)
                    )
                )
            }
        }
}
