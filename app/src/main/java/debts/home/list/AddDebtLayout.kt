package debts.home.list

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.AppCompatAutoCompleteTextView
import android.util.AttributeSet
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding3.widget.textChanges
import debts.common.android.bindView
import debts.home.list.adapter.ContactsAdapter
import debts.home.list.adapter.ContactsItemViewModel
import io.reactivex.disposables.Disposable
import okb.common.android.extension.*
import net.thebix.debts.R

class AddDebtLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val contacts: List<ContactsItemViewModel> = emptyList()
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val avatarView by bindView<ImageView>(R.id.home_debtors_add_debt_avatar)
    private val nameView by bindView<AppCompatAutoCompleteTextView>(R.id.home_debtors_add_debt_name)
    private val amountView by bindView<EditText>(R.id.home_debtors_add_debt_amount)
    private val radioAdd by bindView<RadioGroup>(R.id.home_debtors_add_debt_radio)
    private val commentView by bindView<EditText>(R.id.home_debtors_add_debt_comment)
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
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        nameView.showKeyboard()

        val adapter = ContactsAdapter(context, contacts)
        nameView.setAdapter<ContactsAdapter>(adapter)
        nameView.setOnItemClickListener { _, _, _, id ->
            contact = contacts.firstOrNull { it.id == id }
            Glide.with(avatarView)
                .load(contact?.avatarUrl)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_launcher)
                .fallback(R.drawable.ic_launcher)
                .into(avatarView)
            amountView.requestFocus()
        }

        disposable =
            nameView.textChanges()
                .filter { contact != null }
                .subscribe {
                    contact = null
                    avatarView.setImageResource(R.drawable.ic_launcher)
                }
    }

    override fun onDetachedFromWindow() {
        disposable.dispose()
        super.onDetachedFromWindow()
    }

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
                // TODO: return proper value
                "$",
                commentView.text.trim().toString()

            )
        }

    data class Data(
        val contactId: Long?,
        val name: String,
        val amount: Double,
        val currency: String,
        val comment: String
    )
}