package debts.home.list.mvi

import debts.common.android.mvi.MviAction
import debts.common.android.mvi.MviInitIntention
import debts.common.android.mvi.MviIntention
import debts.common.android.mvi.MviResult
import debts.common.android.mvi.MviState
import debts.common.android.mvi.OneShot
import debts.common.android.mvi.ViewStateWithId
import debts.home.list.adapter.ContactsItemViewModel
import debts.repository.SortType
import debts.usecase.ContactsItemModel

sealed class HomeIntention : MviIntention {

    data class Init(
        val contactPermission: String,
        val requestCode: Int
    ) : HomeIntention(), MviInitIntention

    object InitMenu : HomeIntention()
    data class Filter(val name: String = "") : HomeIntention()
    data class ToggleSortByName(val currentSortType: SortType) : HomeIntention()
    data class ToggleSortByAmount(val currentSortType: SortType) : HomeIntention()

    object OpenSettings : HomeIntention()
    data class ShareAllDebts(val titleText: String) : HomeIntention()

    data class OpenAddDebtDialog(
        val contactPermission: String,
        val requestCode: Int
    ) : HomeIntention()

    data class AddDebt(
        val contactId: Long?,
        val name: String,
        val amount: Double,
        val comment: String

    ) : HomeIntention()

    object SyncWithContacts : HomeIntention()
}

sealed class HomeAction : MviAction {

    data class Init(
        val contactPermission: String,
        val requestCode: Int
    ) : HomeAction()

    object InitMenu : HomeAction()
    data class Filter(val name: String = "") : HomeAction()
    data class SortBy(
        val sortType: SortType = SortType.NOTHING
    ) : HomeAction()

    object OpenSettings : HomeAction()
    data class ShareAllDebts(val titleText: String) : HomeAction()

    data class OpenAddDebtDialog(
        val contactPermission: String,
        val requestCode: Int
    ) : HomeAction()

    data class AddDebt(
        val contactId: Long?,
        val name: String,
        val amount: Double,
        val comment: String
    ) : HomeAction()

    object SyncWithContacts : HomeAction()
}

sealed class HomeResult : MviResult {

    object Error : HomeResult()
    data class SortBy(
        val sortType: SortType = SortType.NOTHING
    ) : HomeResult()

    data class ShowAddDebtDialog(
        val items: List<ContactsItemModel> = emptyList()
    ) : HomeResult()
}

data class HomeState(
    val sortType: OneShot<SortType> = OneShot.empty(),
    val isError: OneShot<Boolean> = OneShot.empty(),
    val contacts: List<ContactsItemViewModel> = emptyList(),
    val showAddDebtDialog: OneShot<Boolean> = OneShot.empty()
) : MviState, ViewStateWithId()
