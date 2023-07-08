package debts.home.list.mvi

import debts.core.common.android.mvi.MviViewModel
import debts.core.common.android.mvi.OneShot
import debts.home.list.TabTypes
import debts.home.list.adapter.toDebtorsItemViewModel
import io.reactivex.functions.BiFunction
import kotlin.math.absoluteValue

class DebtorsViewModel(
    interactor: DebtorsInteractor,
) : MviViewModel<DebtorsIntention, DebtorsAction, DebtorsResult, DebtorsState>(interactor) {

    override val defaultState: DebtorsState
        get() = DebtorsState()

    override fun actionFromIntention(intent: DebtorsIntention): DebtorsAction =
        when (intent) {
            is DebtorsIntention.Init -> DebtorsAction.Init(intent.tabType)
            is DebtorsIntention.RemoveDebtor -> DebtorsAction.RemoveDebtor(intent.debtorId)
            is DebtorsIntention.ShareDebtor -> DebtorsAction.ShareDebtor(
                intent.debtorId,
                intent.titleText,
                intent.borrowedTemplate,
                intent.lentTemplate
            )

            is DebtorsIntention.OpenDetails -> DebtorsAction.OpenDetails(
                intent.debtorId,
                intent.rootId
            )
        }

    override val reducer: BiFunction<DebtorsState, DebtorsResult, DebtorsState>
        get() = BiFunction { prevState, result ->
            when (result) {
                is DebtorsResult.ItemsResult -> {
                    prevState.copy(
                        items = result.items.map { it.toDebtorsItemViewModel() },
                        amountAbs = if (result.tabType == TabTypes.Creditors) result.amount.absoluteValue else result.amount,
                        currency = result.currency
                    )
                }

                DebtorsResult.Error ->
                    prevState.copy(isError = OneShot(true))
            }
        }
}
