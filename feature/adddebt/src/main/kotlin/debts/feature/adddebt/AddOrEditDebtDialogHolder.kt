package debts.feature.adddebt

import androidx.appcompat.app.AppCompatActivity
import debts.feature.contacts.adapter.ContactsItemViewModel
import timber.log.Timber
import java.lang.ref.WeakReference

class AddOrEditDebtDialogHolder(
    activity: AppCompatActivity,
    private val holderCallback: AddOrEditDebtDialogHolderCallback,
) {

    private val activityRef = WeakReference(activity)

    init {
        val existing = activity.supportFragmentManager
            .findFragmentByTag(AddDebtDialogFragment.TAG) as? AddDebtDialogFragment
        existing?.onConfirmListener = { data -> holderCallback.onConfirm(data) }
    }

    fun showAddDebt(
        name: String = "",
        avatarUrl: String = "",
        contacts: List<ContactsItemViewModel> = emptyList(),
        canChangeDebtor: Boolean = true,
    ) {
        Timber.d("showAddDebt(name=$name)")
        show(AddDebtDialogFragment.newAddDebt(name, avatarUrl, contacts, canChangeDebtor))
    }

    fun showEditDebt(
        name: String,
        avatarUrl: String,
        comment: String,
        amount: Double,
        date: Long,
        existingDebtId: Long,
    ) {
        Timber.d("showEditDebt(existingDebtId=$existingDebtId)")
        show(AddDebtDialogFragment.newEditDebt(name, avatarUrl, amount, comment, date, existingDebtId))
    }

    private fun show(fragment: AddDebtDialogFragment) {
        val activity = activityRef.get() ?: return
        fragment.onConfirmListener = { data -> holderCallback.onConfirm(data) }
        val fm = activity.supportFragmentManager
        (fm.findFragmentByTag(AddDebtDialogFragment.TAG) as? AddDebtDialogFragment)?.dismiss()
        fragment.show(fm, AddDebtDialogFragment.TAG)
    }

    interface AddOrEditDebtDialogHolderCallback {

        fun onConfirm(data: DebtLayoutData)
    }
}
