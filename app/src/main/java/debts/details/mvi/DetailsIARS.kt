package debts.details.mvi

import debts.common.android.mvi.MviAction
import debts.common.android.mvi.MviInitIntention
import debts.common.android.mvi.MviIntention
import debts.common.android.mvi.MviResult
import debts.common.android.mvi.MviState
import debts.common.android.mvi.OneShot
import debts.common.android.mvi.ViewStateWithId
import debts.details.adapter.DebtsItemViewModel
import debts.usecase.DebtItemModel

sealed class DetailsIntention : MviIntention {

    data class Init(val id: Long) : DetailsIntention(), MviInitIntention
    data class AddDebt(
        val debtorId: Long,
        val amount: Double,
        val comment: String
    ) : DetailsIntention()

    data class RemoveDebt(val id: Long) : DetailsIntention()
    data class ClearHistory(val id: Long) : DetailsIntention()
    data class RemoveDebtor(val debtorId: Long) : DetailsIntention()
    data class ShareDebtor(
        val debtorId: Long,
        val titleText: String,
        val borrowedTemplate: String,
        val lentTemplate: String
    ) : DetailsIntention()
}

sealed class DetailsAction : MviAction {

    data class Init(val id: Long) : DetailsAction()
    data class AddDebt(
        val debtorId: Long,
        val amount: Double,
        val comment: String

    ) : DetailsAction()

    data class RemoveDebt(val id: Long) : DetailsAction()
    data class ClearHistory(val id: Long) : DetailsAction()
    data class RemoveDebtor(val debtorId: Long) : DetailsAction()
    data class ShareDebtor(
        val debtorId: Long,
        val titleText: String,
        val borrowedTemplate: String,
        val lentTemplate: String
    ) : DetailsAction()
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
