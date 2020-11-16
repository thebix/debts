package debts.adddebt

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import debts.common.android.extensions.showAlert
import debts.home.list.adapter.ContactsItemViewModel
import net.thebix.debts.R
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.Date

class AddOrEditDebtDialogHolder(
    activity: AppCompatActivity,
    private val holderCallbacks: AddOrEditDebtDialogHolderCallbacks
) {

    private val activityRef: WeakReference<AppCompatActivity> = WeakReference(activity)
    private val dialogCallbacks: AddOrAddDebtDialogCallbacks = object : AddOrAddDebtDialogCallbacks {

        override fun onCalendarClicked(data: AddDebtData) {
            addDebtData = data
            dialogAdd?.dismiss()
            dialogAdd = null
            debtLayout = null
            showCalendar()
        }
    }
    private var debtLayout: DebtLayout? = null
    private var dialogAdd: Dialog? = null
    private var addDebtData: AddDebtData? = null

    fun showAddDebt(
        name: String = "",
        avatarUrl: String = "",
        contacts: List<ContactsItemViewModel> = emptyList(),
        canChangeDebtor: Boolean = true
    ) {
        addDebtData = AddDebtData(
            contactId = null,
            avatarUrl = avatarUrl,
            name = name,
            amount = 0.0,
            comment = "",
            existingDebtId = null,
            contacts = contacts,
            date = Date().time,
            canChangeDebtor = canChangeDebtor
        )
        showAddDebtLayout(addDebtData!!)
    }

    fun showEditDebt(
        name: String,
        avatarUrl: String,
        comment: String,
        amount: Double,
        date: Long,
        existingDebtId: Long,
        canChangeDebtor: Boolean = true
    ) {
        addDebtData = AddDebtData(
            contactId = null,
            avatarUrl = avatarUrl,
            name = name,
            amount = amount,
            comment = comment,
            contacts = emptyList(),
            existingDebtId = existingDebtId,
            date = date,
            canChangeDebtor = canChangeDebtor
        )
        showAddDebtLayout(addDebtData!!)
    }

    private fun showAddDebtLayout(addDebtData: AddDebtData) {
        Timber.d("showAddDebtLayout(addDebtData.date=${addDebtData.date})")
        val activity = activityRef.get() ?: return

        debtLayout = DebtLayout(
            activity,
            name = addDebtData.name,
            avatarUrl = addDebtData.avatarUrl,
            comment = addDebtData.comment,
            amount = addDebtData.amount,
            existingDebtId = addDebtData.existingDebtId,
            dialogCallbacks = dialogCallbacks,
            contacts = addDebtData.contacts,
            date = addDebtData.date,
            canChangeDebtor = addDebtData.canChangeDebtor
        )
        dialogAdd = activity.showAlert(
            customView = debtLayout,
            titleResId = if (addDebtData.existingDebtId == null) R.string.home_add_debt_title else R.string.home_add_debt_change_title,
            positiveButtonResId = R.string.home_add_debt_confirm
        ) {
            if (debtLayout == null) return@showAlert
            holderCallbacks.onConfirm(debtLayout!!.data)
        }
    }

    private fun showCalendar() {
        val activity = activityRef.get() ?: return
        val supportFragmentManager = activity.supportFragmentManager

        val calendarPickerBuilder = MaterialDatePicker.Builder
            .datePicker()

        addDebtData?.let {
            calendarPickerBuilder
                .setSelection(it.date)
        }
        val calendarPicker = calendarPickerBuilder
            .build()

        calendarPicker
            .addOnCancelListener {
                Timber.d("addOnCancelListener()")
                addDebtData?.let {
                    showAddDebtLayout(it)
                }
            }
        calendarPicker.addOnNegativeButtonClickListener {
            Timber.d("addOnNegativeButtonClickListener()")
            addDebtData?.let {
                showAddDebtLayout(it)
            }
        }
        calendarPicker.addOnPositiveButtonClickListener { newDate ->
            Timber.d("addOnPositiveButtonClickListener(newDate=$newDate) oldDate=${addDebtData?.date}")
            addDebtData?.let {
                addDebtData = it.copy(date = newDate)
                showAddDebtLayout(addDebtData!!)
            }
        }
        calendarPicker.show(supportFragmentManager, null)
    }

    internal interface AddOrAddDebtDialogCallbacks {

        fun onCalendarClicked(data: AddDebtData)
    }

    interface AddOrEditDebtDialogHolderCallbacks {

        fun onConfirm(data: AddDebtData)
    }
}
