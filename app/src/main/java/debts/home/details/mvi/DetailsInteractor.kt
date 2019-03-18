package debts.home.details.mvi

import debts.common.android.mvi.MviInteractor
import debts.home.usecase.AddDebtUseCase
import debts.home.usecase.ClearHistoryUseCase
import debts.home.usecase.ObserveDebtorUseCase
import debts.home.usecase.ObserveDebtsUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class DetailsInteractor(
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val addDebtUseCase: AddDebtUseCase,
    private val observeDebtorUseCase: ObserveDebtorUseCase,
    private val observeDebtsUseCase: ObserveDebtsUseCase

) : MviInteractor<DetailsAction, DetailsResult> {

    private val initProcessor = ObservableTransformer<DetailsAction.Init, DetailsResult> { actions ->
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

    private val clearHistoryProcessor = ObservableTransformer<DetailsAction.ClearHistory, DetailsResult> { actions ->
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

    private val addDebtProcessor = ObservableTransformer<DetailsAction.AddDebt, DetailsResult> { actions ->
        actions.switchMap {
            addDebtUseCase
                .execute(
                    it.debtorId,
                    null,
                    "",
                    it.amount,
                    it.currency,
                    it.comment
                )
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
                            .compose(addDebtProcessor)
                    )
                )
            }
        }
}
