package debts.home.list.mvi

import debts.common.android.mvi.MviInteractor
import debts.home.usecase.AddDebtUseCase
import debts.home.usecase.RemoveDebtorUseCase
import debts.home.usecase.GetContactsUseCase
import debts.home.usecase.ObserveDebtorsListItemsUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class DebtorsInteractor(
    private val observeDebtorsListItemsUseCase: ObserveDebtorsListItemsUseCase,
    private val getContactsUseCase: GetContactsUseCase,
    private val addDebtUseCase: AddDebtUseCase,
    private val removeDebtorUseCase: RemoveDebtorUseCase
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

    private val getContactsProcessor = ObservableTransformer<DebtorsAction, DebtorsResult> { actions ->
        actions.switchMap {
            getContactsUseCase
                .execute()
                .subscribeOn(Schedulers.io())
                .flatMap { items ->
                    Single.fromCallable { DebtorsResult.Contacts(items) as DebtorsResult }
                }
                .doOnError { Timber.e(it) }
                .onErrorReturnItem(DebtorsResult.Contacts(emptyList()))
                .toObservable()
        }
    }

    private val addDebtProcessor = ObservableTransformer<DebtorsAction.AddDebt, DebtorsResult> { actions ->
        actions.switchMap {
            addDebtUseCase
                .execute(null, it.contactId, it.name, it.amount, it.currency, it.comment)
                .subscribeOn(Schedulers.io())
                .toObservable<DebtorsResult>()
                .doOnError { error -> Timber.e(error) }
                .onErrorReturnItem(DebtorsResult.Error)
        }
    }

    private val filterProcessor = ObservableTransformer<DebtorsAction.Filter, DebtorsResult> { actions ->
        actions.switchMap {
            Observable.fromCallable { DebtorsResult.Filter(it.name.toLowerCase().trim()) }
        }
    }

    private val sortProcessor = ObservableTransformer<DebtorsAction.SortBy, DebtorsResult> { actions ->
        actions.switchMap {
            Observable.fromCallable { DebtorsResult.SortBy(it.sortType) }
        }
    }

    private val removeDebtorProcessor = ObservableTransformer<DebtorsAction.RemoveDebtor, DebtorsResult> { actions ->
        actions.switchMap {
            removeDebtorUseCase
                .execute(it.debtorId)
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
                        action.ofType(DebtorsAction.GetContacts::class.java)
                            .compose(getContactsProcessor),
                        action.ofType(DebtorsAction.AddDebt::class.java)
                            .compose(addDebtProcessor),
                        action.ofType(DebtorsAction.Filter::class.java)
                            .compose(filterProcessor),
                        action.ofType(DebtorsAction.SortBy::class.java)
                            .compose(sortProcessor),
                        action.ofType(DebtorsAction.RemoveDebtor::class.java)
                            .compose(removeDebtorProcessor)
                    )
                )
            }
        }
}
