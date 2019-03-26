package debts.home.details.mvi

import debts.common.android.extensions.toDecimal
import debts.common.android.mvi.MviViewModel
import debts.common.android.mvi.OneShot
import debts.home.details.adapter.toDebtsItemViewModel
import io.reactivex.functions.BiFunction

class DetailsViewModel(
    interactor: DetailsInteractor
) : MviViewModel<DetailsIntention, DetailsAction, DetailsResult, DetailsState>(interactor) {

    override val defaultState: DetailsState
        get() = DetailsState()

    override fun actionFromIntention(intent: DetailsIntention): DetailsAction =
        when (intent) {
            is DetailsIntention.Init -> DetailsAction.Init(intent.id)
            is DetailsIntention.ClearHistory -> DetailsAction.ClearHistory(intent.id)
            is DetailsIntention.AddDebt -> DetailsAction.AddDebt(
                intent.debtorId,
                intent.amount,
                intent.comment
            )
            is DetailsIntention.RemoveDebt -> DetailsAction.RemoveDebt(intent.id)
            is DetailsIntention.RemoveDebtor -> DetailsAction.RemoveDebtor(intent.debtorId)
        }

    override val reducer: BiFunction<DetailsState, DetailsResult, DetailsState>
        get() = BiFunction { prevState, result ->
            when (result) {
                is DetailsResult.History ->
                    prevState.copy(items = result.items
                        .sortedByDescending { it.date }
                        .map { it.toDebtsItemViewModel() })
                DetailsResult.Error ->
                    prevState.copy(isError = OneShot(true))
                is DetailsResult.Debtor ->
                    prevState.copy(
                        name = result.name,
                        amount = result.amount.toDecimal(),
                        currency = result.currency,
                        avatarUrl = result.avatarUrl
                    )
                DetailsResult.DebtorRemoved -> prevState.copy(
                    isDebtorRemoved = OneShot(true)
                )
            }
        }
}
