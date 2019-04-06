package debts.home

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.widget.textChanges
import debts.common.android.extensions.applyLayoutParams
import debts.common.android.extensions.doInRuntime
import debts.common.android.extensions.selfInflate
import debts.common.android.extensions.setPaddingBottomResCompat
import debts.common.android.extensions.setPaddingEndResCompat
import debts.common.android.extensions.setPaddingStartResCompat
import debts.common.android.extensions.setPaddingTopResCompat
import debts.common.android.extensions.showKeyboard
import debts.home.list.adapter.ContactsAdapter
import debts.home.list.adapter.ContactsItemViewModel
import io.reactivex.disposables.CompositeDisposable
import net.thebix.debts.R

class AddDebtLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val contacts: List<ContactsItemViewModel> = emptyList(),
    private val name: String = "",
    private val avatarUrl: String = ""
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private companion object {

        const val AMOUNT_MAX_LENGTH = 16
    }

    val data: Data
        get() {
            val inverseAmount = radioAdd.checkedRadioButtonId == R.id.home_add_debt_radio_subtract
            val amount = try {
                if (amountView.text.length > AMOUNT_MAX_LENGTH)
                    0.0
                else
                    amountView.text.toString().toDouble() * if (inverseAmount) -1 else 1
            } catch (ex: Throwable) {
                0.0
            }
            return Data(
                contact?.id,
                nameView.text.trim().toString(),
                amount,
                commentView.text.trim().toString()

            )
        }

    private val avatarView: ImageView
    private val nameView: AppCompatAutoCompleteTextView
    private val amountLayoutView: TextInputLayout
    private val amountView: EditText
    private val radioAdd: RadioGroup
    private val commentView: EditText
    private var contact: ContactsItemViewModel? = null
    private lateinit var disposables: CompositeDisposable

    init {
        selfInflate(R.layout.home_add_debt_layout)
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
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        nameView.showKeyboard()

        val adapter = ContactsAdapter(context, contacts)
        nameView.setAdapter<ContactsAdapter>(adapter)
        nameView.setOnItemClickListener { _, _, _, id ->
            contact = contacts.firstOrNull { it.id == id }
            showAvatar(contact?.avatarUrl)
            amountView.requestFocus()
        }

        disposables = CompositeDisposable(
            nameView.textChanges()
                .filter { contact != null }
                .subscribe {
                    contact = null
                    avatarView.setImageResource(R.drawable.ic_launcher)
                },
            amountView.textChanges()
                .subscribe {
                    amountLayoutView.error = if (it.length > AMOUNT_MAX_LENGTH) context.getString(R.string.home_add_debt_amount_error) else ""
                }
        )

        if (name.isNotBlank()) {
            nameView.setText(name)
            nameView.isEnabled = false
            amountView.requestFocus()
            amountView.showKeyboard()
        }
        if (avatarUrl.isNotBlank()) {
            showAvatar(avatarUrl)
        }
    }

    override fun onDetachedFromWindow() {
        disposables.dispose()
        super.onDetachedFromWindow()
    }

    private fun showAvatar(url: String?) {
        Glide.with(avatarView)
            .load(url)
            .placeholder(R.drawable.ic_launcher)
            .error(R.drawable.ic_launcher)
            .fallback(R.drawable.ic_launcher)
            .into(avatarView)
    }

    data class Data(
        val contactId: Long?,
        val name: String,
        val amount: Double,
        val comment: String
    )
}
