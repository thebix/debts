package debts.home.list.mvi

import androidx.annotation.IdRes
import debts.common.android.mvi.MviAction
import debts.common.android.mvi.MviInitIntention
import debts.common.android.mvi.MviIntention
import debts.common.android.mvi.MviResult
import debts.common.android.mvi.MviState
import debts.common.android.mvi.OneShot
import debts.common.android.mvi.ViewStateWithId
import debts.home.list.TabTypes
import debts.home.list.adapter.DebtorsItemViewModel
import debts.usecase.DebtorsListItemModel

sealed class DebtorsIntention : MviIntention {

    data class Init(
        val tabType: TabTypes
    ) : DebtorsIntention(), MviInitIntention

    data class RemoveDebtor(val debtorId: Long) : DebtorsIntention()
    data class ShareDebtor(
        val debtorId: Long,
        val titleText: String,
        val borrowedTemplate: String,
        val lentTemplate: String
    ) : DebtorsIntention()

    data class OpenDetails(val debtorId: Long, @IdRes val rootId: Int) : DebtorsIntention()
}

sealed class DebtorsAction : MviAction {

    data class Init(
        val tabType: TabTypes
    ) : DebtorsAction()

    data class RemoveDebtor(val debtorId: Long) : DebtorsAction()
    data class ShareDebtor(
        val debtorId: Long,
        val titleText: String,
        val borrowedTemplate: String,
        val lentTemplate: String
    ) : DebtorsAction()

    data class OpenDetails(val debtorId: Long, @IdRes val rootId: Int) : DebtorsAction()
}

sealed class DebtorsResult : MviResult {

    data class ItemsResult(
        val items: List<DebtorsListItemModel> = emptyList(),
        val amount: Double = 0.0,
        val currency: String = "",
        val tabType: TabTypes = TabTypes.All
    ) : DebtorsResult()

    object Error : DebtorsResult()
}

data class DebtorsState(
    val items: List<DebtorsItemViewModel> = emptyList(),
    val isError: OneShot<Boolean> = OneShot.empty(),
    val amountAbs: Double = 0.0,
    val currency: String = ""
) : MviState, ViewStateWithId()
