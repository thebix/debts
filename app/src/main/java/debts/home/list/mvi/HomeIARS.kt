package debts.home.list.mvi

import debts.common.android.mvi.MviAction
import debts.common.android.mvi.MviIntention
import debts.common.android.mvi.MviResult
import debts.common.android.mvi.MviState
import debts.common.android.mvi.OneShot
import debts.common.android.mvi.ViewStateWithId
import debts.repository.SortType

sealed class HomeIntention : MviIntention {

    object Init : HomeIntention()
    object InitMenu : HomeIntention()
    data class Filter(val name: String = "") : HomeIntention()
    object ToggleSortByName : HomeIntention()
    object ToggleSortByAmount : HomeIntention()

    object OpenSettings : HomeIntention()
    data class ShareAllDebts(val titleText: String) : HomeIntention()

//    data class OpenAddDebtDialog(
//        val contactPermission: String,
//        val requestCode: Int
//    ) : HomeIntention()
//
//    data class AddDebt(
//        val contactId: Long?,
//        val name: String,
//        val amount: Double,
//        val comment: String
//
//    ) : HomeIntention()

}

sealed class HomeAction : MviAction {

    object Init : HomeAction()
    object InitMenu : HomeAction()
    data class Filter(val name: String = "") : HomeAction()
    data class SortBy(
        val sortType: SortType = SortType.NOTHING
    ) : HomeAction()

    object OpenSettings : HomeAction()
    data class ShareAllDebts(val titleText: String) : HomeAction()

//    data class OpenAddDebtDialog(
//        val contactPermission: String,
//        val requestCode: Int
//    ) : HomeAction()
//    data class AddDebt(
//        val contactId: Long?,
//        val name: String,
//        val amount: Double,
//        val comment: String
//    ) : HomeAction()
}

sealed class HomeResult : MviResult {

    object Error : HomeResult()
    data class SortBy(
        val sortType: SortType = SortType.NOTHING
    ) : HomeResult()

//    data class ShowAddDebtDialog(
//        val items: List<ContactsItemModel> = emptyList()
//    ) : HomeResult()
}

data class HomeState(
    val sortType: SortType = SortType.NOTHING,
    val isError: OneShot<Boolean> = OneShot.empty()
//    val contacts: List<ContactsItemViewModel> = emptyList(),
//    val showAddDebtDialog: OneShot<Boolean> = OneShot.empty()
) : MviState, ViewStateWithId() {
}