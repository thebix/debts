package debts.adddebt

import android.content.Context
import android.util.AttributeSet
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
import debts.common.android.extensions.applyLayoutParams
import debts.common.android.extensions.doInRuntime
import debts.common.android.extensions.selfInflate
import debts.common.android.extensions.setPaddingBottomResCompat
import debts.common.android.extensions.setPaddingEndResCompat
import debts.common.android.extensions.setPaddingStartResCompat
import debts.common.android.extensions.setPaddingTopResCompat
import debts.common.android.extensions.showKeyboard
import debts.common.android.extensions.toSimpleDateString
import debts.home.list.adapter.ContactsAdapter
import debts.home.list.adapter.ContactsItemViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import net.thebix.debts.R
import java.util.Date
import kotlin.math.absoluteValue

internal class DebtLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val contacts: List<ContactsItemViewModel> = emptyList(),
    private val name: String = "",
    private var avatarUrl: String = "",
    private val comment: String = "",
    private val amount: Double = 0.0,
    private val existingDebtId: Long? = null,
    private val date: Long = 0L,
    private val canChangeDebtor: Boolean = true,
    private val dialogCallbacks: AddOrEditDebtDialogHolder.AddOrAddDebtDialogCallbacks
) : ScrollView(context, attrs, defStyleAttr) {

    private companion object {

        const val AMOUNT_MAX_LENGTH = 16
    }

    val data: AddDebtData
        get() {
            val inverseAmount = radioAdd.checkedRadioButtonId == R.id.home_add_debt_radio_subtract
            val amount = try {
                if (amountView.text.length > AMOUNT_MAX_LENGTH) 0.0
                else amountView.text.toString().toDouble() * if (inverseAmount) -1 else 1
            } catch (ex: Throwable) {
                0.0
            }
            return AddDebtData(
                contactId = contact?.id,
                name = nameView.text.trim().toString(),
                amount = amount,
                comment = commentView.text.trim().toString(),
                existingDebtId = existingDebtId,
                contacts = contacts,
                avatarUrl = avatarUrl,
                date = date,
                canChangeDebtor = canChangeDebtor
            )
        }

    private val avatarView: ImageView
    private val nameView: AppCompatAutoCompleteTextView
    private val amountLayoutView: TextInputLayout
    private val amountView: EditText
    private val radioAdd: RadioGroup
    private val commentView: EditText
    private val calendarView: TextView
    private val calendarIcon: View
    private var contact: ContactsItemViewModel? = null
    private lateinit var disposables: CompositeDisposable

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
        calendarIcon = findViewById(R.id.home_add_debt_calendar_icon)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        nameView.showKeyboard()

        val adapter = ContactsAdapter(context, contacts)
        nameView.setAdapter<ContactsAdapter>(adapter)
        nameView.setOnItemClickListener { _, _, _, id ->
            contact = contacts.firstOrNull { it.id == id }
            avatarUrl = contact?.avatarUrl ?: ""
            showAvatar(avatarUrl)
            amountView.requestFocus()
        }

        disposables = CompositeDisposable(
            nameView.textChanges()
                .filter { contact != null }
                .subscribe {
                    contact = null
                    avatarView.setImageResource(R.mipmap.ic_launcher)
                },
            amountView.textChanges()
                .subscribe {
                    amountLayoutView.error =
                        if (it.length > AMOUNT_MAX_LENGTH) context.getString(R.string.home_add_debt_amount_error) else ""
                },
            Observable.merge(
                calendarIcon.clicks(),
                calendarView.clicks()
            ).subscribe {
                dialogCallbacks.onCalendarClicked(data)
            }
        )
        initFields()
    }

    override fun onDetachedFromWindow() {
        disposables.dispose()
        super.onDetachedFromWindow()
    }

    private fun initFields() {
        if (name.isNotBlank()) {
            nameView.setText(name)
            amountView.requestFocus()
            amountView.showKeyboard()
        }
        if (canChangeDebtor.not()) {
            nameView.isEnabled = false
        }
        if (avatarUrl.isNotBlank()) {
            showAvatar(avatarUrl)
        }
        if (comment.isNotBlank()) {
            commentView.setText(comment)
        }
        if (amount != 0.0) {
            amountView.setText(amount.absoluteValue.toString())
            if (amount < 0) {
                radioAdd.check(R.id.home_add_debt_radio_subtract)
            }
        }
        calendarView.text = Date(date).toSimpleDateString()
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
}

data class AddDebtData(
    val contactId: Long?,
    val avatarUrl: String,
    val name: String,
    val amount: Double,
    val comment: String,
    val date: Long,
    val existingDebtId: Long?,
    val contacts: List<ContactsItemViewModel>,
    val canChangeDebtor: Boolean
)
