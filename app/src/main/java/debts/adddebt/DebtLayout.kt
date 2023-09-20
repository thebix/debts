package debts.adddebt

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import debts.core.common.android.extensions.applyLayoutParams
import debts.core.common.android.extensions.doInRuntime
import debts.core.common.android.extensions.selfInflate
import debts.core.common.android.extensions.setPaddingBottomResCompat
import debts.core.common.android.extensions.setPaddingEndResCompat
import debts.core.common.android.extensions.setPaddingStartResCompat
import debts.core.common.android.extensions.setPaddingTopResCompat
import debts.core.common.android.extensions.showKeyboard
import debts.core.common.android.extensions.toSimpleDateString
import debts.home.list.adapter.ContactsAdapter
import debts.home.list.adapter.ContactsItemViewModel
import io.reactivex.disposables.CompositeDisposable
import net.thebix.debts.R
import java.util.Date
import kotlin.math.absoluteValue

internal class DebtLayout(context: Context) : ScrollView(context) {

    private companion object {

        const val AMOUNT_MAX_LENGTH = 16
    }

    val data: DebtLayoutData
        get() {
            val inverseAmount = radioAdd.checkedRadioButtonId == R.id.home_add_debt_radio_subtract
            val amount = runCatching {
                if (amountView.text.length > AMOUNT_MAX_LENGTH) {
                    0.0
                } else {
                    amountView.text.toString().toDouble() * if (inverseAmount) -1 else 1
                }
            }.getOrDefault(0.0)
            return DebtLayoutData(
                contactId = params.contactId,
                name = nameView.text.trim().toString(),
                amount = amount,
                comment = commentView.text.trim().toString(),
                existingDebtId = params.existingDebtId,
                date = params.date
            )
        }

    private val avatarView: ImageView
    private val nameView: AppCompatAutoCompleteTextView
    private val amountLayoutView: TextInputLayout
    private val amountView: EditText
    private val radioAdd: RadioGroup
    private val commentView: EditText
    private val calendarView: TextView
    private val calendarContainer: View

    private lateinit var disposables: CompositeDisposable
    private lateinit var params: DebtLayoutParams
    private lateinit var dialogCallback: DebtLayoutCallback

    init {
        selfInflate(R.layout.add_or_edit_debt_layout)
        doInRuntime {
            applyLayoutParams()
            setPaddingTopResCompat(R.dimen.padding_20dp)
            setPaddingBottomResCompat(R.dimen.padding_20dp)
            setPaddingStartResCompat(R.dimen.padding_20dp)
            setPaddingEndResCompat(R.dimen.padding_20dp)
        }
        avatarView = findViewById(R.id.home_add_debt_avatar)
        nameView = findViewById(R.id.home_add_debt_name)
        amountLayoutView = findViewById(R.id.home_add_debt_amount_layout)
        amountView = findViewById(R.id.home_add_debt_amount)
        radioAdd = findViewById(R.id.home_add_debt_radio)
        commentView = findViewById(R.id.home_add_debt_comment)
        calendarView = findViewById(R.id.home_add_debt_calendar)
        calendarContainer = findViewById(R.id.home_add_debt_calendar_container)
    }

    fun setup(params: DebtLayoutParams, dialogCallback: DebtLayoutCallback) {
        this.params = params
        this.dialogCallback = dialogCallback
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        nameView.showKeyboard()
        initFields()

        val adapter = ContactsAdapter(context, params.contacts)
        nameView.setAdapter<ContactsAdapter>(adapter)
        nameView.setOnItemClickListener { _, _, _, id ->
            val contact = params.contacts.firstOrNull { it.id == id }
            params = params.copy(avatarUrl = contact?.avatarUrl ?: "", contactId = id)
            showAvatar(params.avatarUrl)
            amountView.requestFocus()
        }

        disposables = CompositeDisposable(
            nameView.textChanges()
                .filter { params.contactId != null && nameView.hasFocus() }
                .subscribe {
                    params = params.copy(contactId = null, avatarUrl = "")
                    avatarView.setImageResource(R.mipmap.ic_launcher)
                },
            amountView.textChanges()
                .subscribe {
                    amountLayoutView.error =
                        if (it.length > AMOUNT_MAX_LENGTH) context.getString(R.string.home_add_debt_amount_error) else ""
                },
            calendarContainer.clicks()
                .subscribe {
                    val params = params.copy(
                        name = data.name,
                        amount = data.amount,
                        comment = data.comment
                    )
                    dialogCallback.onCalendarClicked(params)
                }
        )
    }

    override fun onDetachedFromWindow() {
        disposables.dispose()
        super.onDetachedFromWindow()
    }

    private fun initFields() {
        if (params.name.isNotBlank()) {
            nameView.setText(params.name)
            amountView.requestFocus()
            amountView.showKeyboard()
        }
        if (params.canChangeDebtor.not()) {
            nameView.isEnabled = false
        }
        if (params.avatarUrl.isNotBlank()) {
            showAvatar(params.avatarUrl)
        }
        if (params.comment.isNotBlank()) {
            commentView.setText(params.comment)
        }
        if (params.amount != 0.0) {
            amountView.setText(params.amount.absoluteValue.toString())
            if (params.amount < 0) {
                radioAdd.check(R.id.home_add_debt_radio_subtract)
            }
        }
        calendarView.text = Date(params.date).toSimpleDateString()
    }

    private fun showAvatar(url: String?) {
        Glide.with(avatarView)
            .load(if (url.isNullOrBlank()) R.mipmap.ic_launcher else url)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .fallback(R.mipmap.ic_launcher)
            .apply(RequestOptions.circleCropTransform())
            .into(avatarView)
    }

    interface DebtLayoutCallback {

        fun onCalendarClicked(params: DebtLayoutParams)
    }

    data class DebtLayoutParams(
        val contactId: Long? = null,
        val avatarUrl: String = "",
        val name: String = "",
        val amount: Double = 0.0,
        val comment: String = "",
        val date: Long = Long.MIN_VALUE,
        val existingDebtId: Long? = null,
        val contacts: List<ContactsItemViewModel> = emptyList(),
        val canChangeDebtor: Boolean = true
    )
}
