package debts.home.details.adapter

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import debts.common.android.adapters.ItemRenderer
import debts.common.android.extensions.toDecimal
import debts.common.android.extensions.toSimpleDateTimeString
import okb.common.android.extension.*
import java.util.*
import kotlin.math.absoluteValue
import net.thebix.debts.R

class DebtItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr),
    ItemRenderer<DebtsItemViewModel> {

    private val signView: TextView
    private val amountView: TextView
    private val dateView: TextView
    private val commentView: TextView

    init {
        selfInflate(R.layout.home_details_debts_item_layout)
        doInRuntime {
            applyLayoutParams()
            setPaddingTopResCompat(R.dimen.padding_16dp)
            setPaddingBottomResCompat(R.dimen.padding_16dp)

        }
        signView = findViewById(R.id.home_details_debts_item_sign)
        amountView = findViewById(R.id.home_details_debts_item_amount)
        dateView = findViewById(R.id.home_details_debts_item_date)
        commentView = findViewById(R.id.home_details_debts_item_comment)
    }

    override fun render(data: DebtsItemViewModel) {
        if (data is DebtsItemViewModel.DebtItemViewModel) {
            with(data) {
                signView.text = context.getString(
                    if (amount < 0) R.string.home_debtors_item_amount_borrowed else R.string.home_details_debts_item_sign_lent
                )
                amountView.text = resources.getString(
                    R.string.home_details_debt_amount,
                    amount.toDecimal().absoluteValue,
                    currency
                )
                dateView.text = Date(date).toSimpleDateTimeString()
                commentView.visible = comment.isNotBlank()
                commentView.text = comment
            }
        }
    }
}
