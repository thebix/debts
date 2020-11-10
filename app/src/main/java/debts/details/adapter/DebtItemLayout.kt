package debts.details.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import debts.common.android.adapters.ItemRenderer
import debts.common.android.extensions.applyLayoutParams
import debts.common.android.extensions.doInRuntime
import debts.common.android.extensions.selfInflate
import debts.common.android.extensions.setPaddingBottomResCompat
import debts.common.android.extensions.setPaddingEndResCompat
import debts.common.android.extensions.setPaddingStartResCompat
import debts.common.android.extensions.setPaddingTopResCompat
import debts.common.android.extensions.setSelectableItemBackground
import debts.common.android.extensions.showPopup
import debts.common.android.extensions.toFormattedCurrency
import debts.common.android.extensions.toSimpleDateTimeString
import debts.common.android.extensions.visible
import net.thebix.debts.R
import java.util.Date
import kotlin.math.absoluteValue

@SuppressLint("ViewConstructor")
class DebtItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    historyItemCallback: HistoryItemCallback
) : ConstraintLayout(context, attrs, defStyleAttr),
    ItemRenderer<DebtsItemViewModel> {

    private val signView: TextView
    private val amountView: TextView
    private val dateView: TextView
    private val commentView: TextView
    private var debtId: Long = 0

    init {
        selfInflate(R.layout.details_debts_item_layout)
        doInRuntime {
            applyLayoutParams()
            setPaddingTopResCompat(R.dimen.padding_16dp)
            setPaddingBottomResCompat(R.dimen.padding_16dp)
            setPaddingStartResCompat(R.dimen.padding_64dp)
            setPaddingEndResCompat(R.dimen.padding_8dp)
            setSelectableItemBackground()
        }
        signView = findViewById(R.id.details_debts_item_sign)
        amountView = findViewById(R.id.details_debts_item_amount)
        dateView = findViewById(R.id.details_debts_item_date)
        commentView = findViewById(R.id.details_debts_item_comment)
        this.setOnLongClickListener {
            this.showPopup(
                R.menu.home_details_debts_item_menu,
                PopupMenu.OnMenuItemClickListener { menuItem ->
                    when (menuItem?.itemId) {
                        R.id.home_details_debts_item_menu_remove -> {
                            historyItemCallback.onDebtRemove(debtId)
                            true
                        }
                        R.id.home_details_debts_item_menu_chage -> {
                            historyItemCallback.onDebtEdit(debtId)
                            true
                        }
                        else -> false
                    }
                })
            true
        }
    }

    override fun render(data: DebtsItemViewModel) {
        if (data is DebtsItemViewModel.DebtItemViewModel) {
            with(data) {
                debtId = id
                signView.text = context.getString(
                    if (amount < 0) R.string.details_debts_item_sign_borrowed else R.string.details_debts_item_sign_lent
                )
                amountView.text = resources.getString(
                    R.string.details_debt_amount,
                    currency,
                    amount.absoluteValue.toFormattedCurrency()
                )
                dateView.text = Date(date).toSimpleDateTimeString()
                commentView.visible = comment.isNotBlank()
                commentView.text = comment
            }
        }
    }

    interface HistoryItemCallback {

        fun onDebtRemove(debtId: Long)
        fun onDebtEdit(debtId: Long)
    }
}
