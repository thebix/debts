package debts.home.list.mvi

import debts.common.android.mvi.MviInteractor
import debts.home.usecase.ObserveDebtorsListItemsUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class DebtorsInteractor(
    private val observeDebtorsListItemsUseCase: ObserveDebtorsListItemsUseCase
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

    override fun actionProcessor(): ObservableTransformer<in DebtorsAction, out DebtorsResult> =
        ObservableTransformer { actions ->
            actions.publish { action ->
                Observable.merge(
                    listOf(
                        action.ofType(DebtorsAction.Init::class.java)
                            .compose(initProcessor)
                    )
                )
            }
        }
}
