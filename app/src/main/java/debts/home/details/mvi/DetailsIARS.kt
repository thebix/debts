package debts.home.details.mvi

import debts.common.android.mvi.*
import debts.home.details.adapter.DebtsItemViewModel
import debts.home.list.mvi.DebtorsIntention
import debts.home.usecase.DebtItemModel

sealed class DetailsIntention : MviIntention {

    data class Init(val id: Long) : DetailsIntention(), MviInitIntention
    data class AddDebt(
        val debtorId: Long,
        val amount: Double,
        val currency: String,
        val comment: String

    ) : DetailsIntention()

    data class RemoveDebt(val id: Long) : DetailsIntention()
    data class ClearHistory(val id: Long) : DetailsIntention()
    data class RemoveDebtor(val debtorId: Long) : DetailsIntention()
}

sealed class DetailsAction : MviAction {

    data class Init(val id: Long) : DetailsAction()
    data class AddDebt(
        val debtorId: Long,
        val amount: Double,
        val currency: String,
        val comment: String

    ) : DetailsAction()

    data class RemoveDebt(val id: Long) : DetailsAction()
    data class ClearHistory(val id: Long) : DetailsAction()
    data class RemoveDebtor(val debtorId: Long) : DetailsAction()
}

sealed class DetailsResult : MviResult {

    data class Debtor(
        val name: String,
        val amount: Double,
        val currency: String,
        val avatarUrl: String
    ) : DetailsResult()

    data class History(
        val items: List<DebtItemModel> = emptyList()
    ) : DetailsResult()

    object DebtorRemoved : DetailsResult()

    object Error : DetailsResult()
}

data class DetailsState(
    val items: List<DebtsItemViewModel.DebtItemViewModel> = emptyList(),
    val name: String = "",
    val amount: Double = 0.0,
    val currency: String = "",
    val avatarUrl: String = "",
    val isError: OneShot<Boolean> = OneShot.empty(),
    val isDebtorRemoved: OneShot<Boolean> = OneShot.empty()
) : MviState, ViewStateWithId()
