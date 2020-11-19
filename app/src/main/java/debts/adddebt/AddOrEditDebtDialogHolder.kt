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
    private val holderCallback: AddOrEditDebtDialogHolderCallback
) {

    private val activityRef: WeakReference<AppCompatActivity> = WeakReference(activity)
    private val dialogCallbacks: DebtLayout.DebtLayoutCallback = object : DebtLayout.DebtLayoutCallback {

        override fun onCalendarClicked(params: DebtLayout.DebtLayoutParams) {
            debtLayoutParams = params
            dialogAdd?.dismiss()
            dialogAdd = null
            debtLayout = null
            showCalendar()
        }
    }
    private var debtLayout: DebtLayout? = null
    private var dialogAdd: Dialog? = null
    private var debtLayoutParams: DebtLayout.DebtLayoutParams = DebtLayout.DebtLayoutParams()

    fun showAddDebt(
        name: String = "",
        avatarUrl: String = "",
        contacts: List<ContactsItemViewModel> = emptyList(),
        canChangeDebtor: Boolean = true
    ) {
        val debtLayoutParams = DebtLayout.DebtLayoutParams(
            avatarUrl = avatarUrl,
            name = name,
            contacts = contacts,
            date = Date().time,
            canChangeDebtor = canChangeDebtor
        )
        showAddDebtLayout(debtLayoutParams)
    }

    fun showEditDebt(
        name: String,
        avatarUrl: String,
        comment: String,
        amount: Double,
        date: Long,
        existingDebtId: Long
    ) {
        val debtLayoutParams = DebtLayout.DebtLayoutParams(
            avatarUrl = avatarUrl,
            name = name,
            amount = amount,
            comment = comment,
            existingDebtId = existingDebtId,
            date = date,
            canChangeDebtor = false
        )
        showAddDebtLayout(debtLayoutParams)
    }

    private fun showAddDebtLayout(params: DebtLayout.DebtLayoutParams) {
        Timber.d("showAddDebtLayout(params.date=${params.date})")
        val activity = activityRef.get() ?: return

        debtLayout = DebtLayout(activity).apply {
            setup(params, dialogCallbacks)
        }
        dialogAdd = activity.showAlert(
            customView = debtLayout,
            titleResId = if (params.existingDebtId == null) R.string.home_add_debt_title else R.string.home_add_debt_change_title,
            positiveButtonResId = R.string.home_add_debt_confirm
        ) {
            val data = debtLayout?.data ?: return@showAlert
            holderCallback.onConfirm(data)
            dialogAdd = null
            debtLayout = null
        }
    }

    private fun showCalendar() {
        val activity = activityRef.get() ?: return
        val supportFragmentManager = activity.supportFragmentManager

        val calendarPickerBuilder = MaterialDatePicker.Builder
            .datePicker()

        if (debtLayoutParams.date != Long.MIN_VALUE) {
            calendarPickerBuilder
                .setSelection(debtLayoutParams.date)
        }
        val calendarPicker = calendarPickerBuilder
            .build()

        calendarPicker
            .addOnCancelListener {
                Timber.d("addOnCancelListener()")
                showAddDebtLayout(debtLayoutParams)
            }
        calendarPicker.addOnNegativeButtonClickListener {
            Timber.d("addOnNegativeButtonClickListener()")
            showAddDebtLayout(debtLayoutParams)
        }
        calendarPicker.addOnPositiveButtonClickListener { newDate ->
            Timber.d("addOnPositiveButtonClickListener(newDate=$newDate) oldDate=${debtLayoutParams.date}")
            debtLayoutParams = debtLayoutParams.copy(date = newDate)
            showAddDebtLayout(debtLayoutParams)
        }
        calendarPicker.show(supportFragmentManager, null)
    }

    interface AddOrEditDebtDialogHolderCallback {

        fun onConfirm(data: DebtLayoutData)
    }
}
