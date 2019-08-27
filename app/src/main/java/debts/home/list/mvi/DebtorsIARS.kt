package debts.home.list.mvi

import androidx.annotation.IdRes
import debts.common.android.mvi.MviAction
import debts.common.android.mvi.MviIntention
import debts.common.android.mvi.MviResult
import debts.common.android.mvi.MviState
import debts.common.android.mvi.OneShot
import debts.common.android.mvi.ViewStateWithId
import debts.home.list.TabTypes
import debts.home.list.adapter.ContactsItemViewModel
import debts.home.list.adapter.DebtorsItemViewModel
import debts.repository.SortType
import debts.usecase.ContactsItemModel
import debts.usecase.DebtorsListItemModel

sealed class DebtorsIntention : MviIntention {

    data class Init(
        val contactPermission: String,
        val requestCode: Int,
        val tabType: TabTypes
    ) : DebtorsIntention()

    data class OpenAddDebtDialog(
        val contactPermission: String,
        val requestCode: Int
    ) : DebtorsIntention()

    data class AddDebt(
        val contactId: Long?,
        val name: String,
        val amount: Double,
        val comment: String

    ) : DebtorsIntention()

    object ToggleSortByName : DebtorsIntention()
    object ToggleSortByAmount : DebtorsIntention()
    data class RemoveDebtor(val debtorId: Long) : DebtorsIntention()
    data class ShareDebtor(
        val debtorId: Long,
        val titleText: String,
        val borrowedTemplate: String,
        val lentTemplate: String
    ) : DebtorsIntention()

    data class OpenDetails(val debtorId: Long, @IdRes val rootId: Int) : DebtorsIntention()
    object SyncWithContacts : DebtorsIntention()

    object OpenSettings : DebtorsIntention()
    data class ShareAllDebts(val titleText: String) : DebtorsIntention()
}

sealed class DebtorsAction : MviAction {

    data class Init(
        val contactPermission: String,
        val requestCode: Int,
        val tabType: TabTypes
    ) : DebtorsAction()

    data class OpenAddDebtDialog(
        val contactPermission: String,
        val requestCode: Int
    ) : DebtorsAction()

    data class AddDebt(
        val contactId: Long?,
        val name: String,
        val amount: Double,
        val comment: String
    ) : DebtorsAction()

    data class SortBy(
        val sortType: SortType = SortType.NOTHING
    ) : DebtorsAction()

    data class RemoveDebtor(val debtorId: Long) : DebtorsAction()
    data class ShareDebtor(
        val debtorId: Long,
        val titleText: String,
        val borrowedTemplate: String,
        val lentTemplate: String
    ) : DebtorsAction()

    data class OpenDetails(val debtorId: Long, @IdRes val rootId: Int) : DebtorsAction()
    object SyncWithContacts : DebtorsAction()

    object OpenSettings : DebtorsAction()
    data class ShareAllDebts(val titleText: String) : DebtorsAction()
}

sealed class DebtorsResult : MviResult {

    data class ItemsResult(
        val items: List<DebtorsListItemModel> = emptyList(),
        val tabType: TabTypes = TabTypes.All
    ) : DebtorsResult()

    object Error : DebtorsResult()
    data class ShowAddDebtDialog(
        val items: List<ContactsItemModel> = emptyList()
    ) : DebtorsResult()

    data class SortBy(
        val sortType: SortType = SortType.NOTHING
    ) : DebtorsResult()
}

data class DebtorsState(
    val items: List<DebtorsItemViewModel> = emptyList(),
    val isError: OneShot<Boolean> = OneShot.empty(),
    val showAddDebtDialog: OneShot<Boolean> = OneShot.empty(),
    val contacts: List<ContactsItemViewModel> = emptyList(),
    val nameFilter: String = "",
    val sortType: SortType = SortType.NOTHING,
    val headerIndexes: List<Int> = emptyList()
) : MviState, ViewStateWithId()
