package debts.details.mvi

import debts.common.android.mvi.MviViewModel
import debts.common.android.mvi.OneShot
import debts.details.adapter.toDebtsItemViewModel
import io.reactivex.functions.BiFunction
import kotlin.math.absoluteValue

class DetailsViewModel(
    interactor: DetailsInteractor,
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
                intent.comment,
                intent.date
            )

            is DetailsIntention.RemoveDebt -> DetailsAction.RemoveDebt(intent.id)
            is DetailsIntention.EditDebt -> DetailsAction.EditDebt(intent.id)
            is DetailsIntention.RemoveDebtor -> DetailsAction.RemoveDebtor(intent.debtorId)
            is DetailsIntention.ShareDebtor -> DetailsAction.ShareDebtor(
                intent.debtorId,
                intent.titleText,
                intent.borrowedTemplate,
                intent.lentTemplate
            )

            is DetailsIntention.EditDebtSave -> DetailsAction.EditDebtSave(intent.debtId, intent.amount, intent.comment, intent.date)
        }

    override val reducer: BiFunction<DetailsState, DetailsResult, DetailsState>
        get() = BiFunction { prevState, result ->
            when (result) {
                is DetailsResult.History ->
                    prevState.copy(
                        items = result.items
                            .sortedByDescending { it.date }
                            .map { it.toDebtsItemViewModel() }
                    )

                DetailsResult.Error ->
                    prevState.copy(isError = OneShot(true))

                is DetailsResult.Debtor ->
                    prevState.copy(
                        name = result.name,
                        amount = result.amount.absoluteValue,
                        currency = result.currency,
                        avatarUrl = result.avatarUrl
                    )

                is DetailsResult.EditDebt -> prevState.copy(
                    debtEdit = OneShot(
                        DetailsState.EditDebt(
                            debtId = result.debtId,
                            amount = result.amount,
                            comment = result.comment,
                            date = result.date
                        )
                    )
                )

                DetailsResult.DebtorRemoved -> prevState.copy(
                    isDebtorRemoved = OneShot(true)
                )
            }
        }
}
