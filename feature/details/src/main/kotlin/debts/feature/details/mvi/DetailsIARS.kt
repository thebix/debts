package debts.feature.details.mvi

import debts.core.common.android.mvi.MviAction
import debts.core.common.android.mvi.MviInitIntention
import debts.core.common.android.mvi.MviIntention
import debts.core.common.android.mvi.MviResult
import debts.core.common.android.mvi.MviState
import debts.core.common.android.mvi.OneShot
import debts.core.common.android.mvi.ViewStateWithId
import debts.feature.details.adapter.DebtsItemViewModel
import debts.core.usecase.data.DebtItemModel

sealed class DetailsIntention : MviIntention {

    data class Init(val id: Long) : DetailsIntention(), MviInitIntention
    data class AddDebt(
        val debtorId: Long,
        val amount: Double,
        val comment: String,
        val date: Long,
    ) : DetailsIntention()

    data class RemoveDebt(val id: Long) : DetailsIntention()
    data class EditDebt(val id: Long) : DetailsIntention()
    data class EditDebtSave(
        val debtId: Long,
        val amount: Double,
        val comment: String,
        val date: Long,
    ) : DetailsIntention()

    data class ClearHistory(val id: Long) : DetailsIntention()
    data class RemoveDebtor(val debtorId: Long) : DetailsIntention()
    data class ShareDebtor(
        val debtorId: Long,
        val titleText: String,
        val borrowedTemplate: String,
        val lentTemplate: String,
    ) : DetailsIntention()
}

sealed class DetailsAction : MviAction {

    data class Init(val id: Long) : DetailsAction()
    data class AddDebt(
        val debtorId: Long,
        val amount: Double,
        val comment: String,
        val date: Long,

        ) : DetailsAction()

    data class RemoveDebt(val id: Long) : DetailsAction()
    data class EditDebt(val id: Long) : DetailsAction()
    data class EditDebtSave(
        val debtId: Long,
        val amount: Double,
        val comment: String,
        val date: Long,
    ) : DetailsAction()

    data class ClearHistory(val id: Long) : DetailsAction()
    data class RemoveDebtor(val debtorId: Long) : DetailsAction()
    data class ShareDebtor(
        val debtorId: Long,
        val titleText: String,
        val borrowedTemplate: String,
        val lentTemplate: String,
    ) : DetailsAction()
}

sealed class DetailsResult : MviResult {

    data class Debtor(
        val name: String,
        val amount: Double,
        val currency: String,
        val avatarUrl: String,
    ) : DetailsResult()

    data class History(
        val items: List<DebtItemModel> = emptyList(),
    ) : DetailsResult()

    data class EditDebt(
        val debtId: Long,
        val amount: Double,
        val comment: String,
        val date: Long,
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
    val isDebtorRemoved: OneShot<Boolean> = OneShot.empty(),
    val debtEdit: OneShot<EditDebt> = OneShot.empty(),
) : MviState, ViewStateWithId() {

    data class EditDebt(
        val debtId: Long = 0L,
        val comment: String = "",
        val amount: Double = 0.0,
        val date: Long = 0L,
    )
}
