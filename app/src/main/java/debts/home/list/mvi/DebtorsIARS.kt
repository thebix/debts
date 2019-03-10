package debts.home.list.mvi

import debts.common.android.mvi.*
import debts.home.list.adapter.DebtorsItemViewModel
import debts.home.usecase.DebtorsListItemModel

sealed class DebtorsIntention : MviIntention {

    object Init : DebtorsIntention(), MviInitIntention
}

sealed class DebtorsAction : MviAction {

    object Init : DebtorsAction()
}

sealed class DebtorsResult : MviResult {

    data class ItemsResult(
        val items: List<DebtorsListItemModel> = emptyList()
    ) : DebtorsResult()

    object Error : DebtorsResult()
}

data class DebtorsState(
    val items: List<DebtorsItemViewModel.DebtorItemViewModel> = emptyList(),
    val isError: OneShot<Boolean> = OneShot.empty()
) : MviState, ViewStateWithId()
