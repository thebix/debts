package debts.home.details.adapter

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import debts.common.android.adapters.ItemRenderer
import debts.common.android.bindView
import debts.common.android.extensions.toDecimal
import debts.common.android.extensions.toSimpleDateString
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

    private val signView by bindView<TextView>(R.id.home_details_debts_item_sign)
    private val amountView by bindView<TextView>(R.id.home_details_debts_item_amount)
    private val dateView by bindView<TextView>(R.id.home_details_debts_item_date)
    private val commentView by bindView<TextView>(R.id.home_details_debts_item_comment)

    init {
        selfInflate(R.layout.home_details_debts_item_layout)
        doInRuntime {
            applyLayoutParams()
            setPaddingTopResCompat(R.dimen.padding_16dp)
            setPaddingBottomResCompat(R.dimen.padding_16dp)
        }
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
                dateView.text = Date(date).toSimpleDateString()
                commentView.visible = comment.isNotBlank()
                commentView.text = comment
            }
        }
    }
}
