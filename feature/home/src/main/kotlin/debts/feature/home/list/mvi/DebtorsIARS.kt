package debts.feature.home.list.mvi

import androidx.annotation.IdRes
import debts.core.common.android.mvi.MviAction
import debts.core.common.android.mvi.MviInitIntention
import debts.core.common.android.mvi.MviIntention
import debts.core.common.android.mvi.MviResult
import debts.core.common.android.mvi.MviState
import debts.core.common.android.mvi.OneShot
import debts.core.common.android.mvi.ViewStateWithId
import debts.feature.home.list.adapter.DebtorsItemViewModel
import debts.core.usecase.data.DebtorsListItemModel
import debts.core.usecase.data.TabTypes

sealed class DebtorsIntention : MviIntention {

    data class Init(
        val tabType: TabTypes,
    ) : DebtorsIntention(), MviInitIntention

    data class RemoveDebtor(val debtorId: Long) : DebtorsIntention()
    data class ShareDebtor(
        val debtorId: Long,
        val titleText: String,
        val borrowedTemplate: String,
        val lentTemplate: String,
    ) : DebtorsIntention()

    data class OpenDetails(val debtorId: Long, @IdRes val rootId: Int) : DebtorsIntention()
}

sealed class DebtorsAction : MviAction {

    data class Init(
        val tabType: TabTypes,
    ) : DebtorsAction()

    data class RemoveDebtor(val debtorId: Long) : DebtorsAction()
    data class ShareDebtor(
        val debtorId: Long,
        val titleText: String,
        val borrowedTemplate: String,
        val lentTemplate: String,
    ) : DebtorsAction()

    data class OpenDetails(val debtorId: Long, @IdRes val rootId: Int) : DebtorsAction()
}

sealed class DebtorsResult : MviResult {

    data class ItemsResult(
        val items: List<DebtorsListItemModel> = emptyList(),
        val amount: Double = 0.0,
        val currency: String = "",
        val tabType: TabTypes = TabTypes.All,
    ) : DebtorsResult()

    object Error : DebtorsResult()
}

data class DebtorsState(
    val items: List<DebtorsItemViewModel> = emptyList(),
    val isError: OneShot<Boolean> = OneShot.empty(),
    val amountAbs: Double = 0.0,
    val currency: String = "",
) : MviState, ViewStateWithId()
