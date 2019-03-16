package debts.home.list.adapter

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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

    private val avatarView by bindView<ImageView>(R.id.home_debtors_item_avatar)
    private val nameView by bindView<TextView>(R.id.home_debtors_item_name)
    private val amountView by bindView<TextView>(R.id.home_debtors_item_amount)
    private val dateView by bindView<TextView>(R.id.home_debtors_item_date)

    init {
        selfInflate(R.layout.home_debtors_item_layout)
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
                if (avatarUrl.isNotBlank()) {
                    Glide.with(avatarView)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_launcher)
                        .error(R.drawable.ic_launcher)
                        .fallback(R.drawable.ic_launcher)
                        .apply(RequestOptions.circleCropTransform())
                        .into(avatarView)
                }
            }
        }
    }
}
