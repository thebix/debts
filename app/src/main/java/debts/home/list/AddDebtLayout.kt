package debts.home.list

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import android.util.AttributeSet
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding3.widget.textChanges
import debts.common.android.extensions.*
import debts.home.list.adapter.ContactsAdapter
import debts.home.list.adapter.ContactsItemViewModel
import io.reactivex.disposables.Disposable
import net.thebix.debts.R

class AddDebtLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val contacts: List<ContactsItemViewModel> = emptyList(),
    private val name: String = "",
    private val avatarUrl: String = ""
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val data: Data
        get() {
            val inverseAmount = radioAdd.checkedRadioButtonId == R.id.home_debtors_add_debt_radio_subtract
            val amount = try {
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
    private val amountView: EditText
    private val radioAdd: RadioGroup
    private val commentView: EditText
    private var contact: ContactsItemViewModel? = null
    private lateinit var disposable: Disposable

    init {
        selfInflate(R.layout.home_debtors_add_debt_layout)
        doInRuntime {
            applyLayoutParams()
            setPaddingTopResCompat(R.dimen.padding_20dp)
            setPaddingBottomResCompat(R.dimen.padding_20dp)
            setPaddingStartResCompat(R.dimen.padding_20dp)
            setPaddingEndResCompat(R.dimen.padding_20dp)
        }
        avatarView = findViewById(R.id.home_debtors_add_debt_avatar)
        nameView = findViewById(R.id.home_debtors_add_debt_name)
        amountView = findViewById(R.id.home_debtors_add_debt_amount)
        radioAdd = findViewById(R.id.home_debtors_add_debt_radio)
        commentView = findViewById(R.id.home_debtors_add_debt_comment)
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

        disposable =
            nameView.textChanges()
                .filter { contact != null }
                .subscribe {
                    contact = null
                    avatarView.setImageResource(R.drawable.ic_launcher)
                }

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
        disposable.dispose()
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
