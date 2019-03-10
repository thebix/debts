package debts.home.list.adapter

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import debts.common.android.adapters.ItemRenderer
import debts.common.android.bindView
import debts.common.android.extensions.toDecimal
import debts.common.android.extensions.toSimpleDateString
import okb.common.android.extension.*
import java.util.*
import net.thebix.debts.R

class DebtorItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr),
    ItemRenderer<DebtorsItemViewModel> {

    private val nameView by bindView<TextView>(R.id.home_debtors_item_name)
    private val amountView by bindView<TextView>(R.id.home_debtors_item_amount)
    private val dateView by bindView<TextView>(R.id.home_debtors_item_date)

    init {
        View.inflate(context, R.layout.home_debtors_item_layout, this)

        doInRuntime {
            applyLayoutParams()
            setPaddingTopResCompat(R.dimen.padding_16dp)
            setPaddingBottomResCompat(R.dimen.padding_16dp)
        }
    }

    override fun render(data: DebtorsItemViewModel) {
        if (data is DebtorsItemViewModel.DebtorItemViewModel) {
            with(data) {
                nameView.text = name
                amountView.text = resources.getString(
                    R.string.home_debtors_item_amount,
                    amount.toDecimal(),
                    currency
                )
                dateView.visible = lastDate > 0
                dateView.text = resources.getString(
                    R.string.home_debtors_item_date,
                    Date(lastDate).toSimpleDateString()
                )
            }
        }
    }
}
