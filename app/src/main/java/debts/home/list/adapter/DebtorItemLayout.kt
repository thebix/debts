package debts.home.list.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import debts.core.common.android.adapters.ItemRenderer
import debts.core.common.android.extensions.applyLayoutParams
import debts.core.common.android.extensions.doInRuntime
import debts.core.common.android.extensions.selfInflate
import debts.core.common.android.extensions.setPaddingBottomResCompat
import debts.core.common.android.extensions.setPaddingStartResCompat
import debts.core.common.android.extensions.setPaddingTopResCompat
import debts.core.common.android.extensions.setSelectableItemBackground
import debts.core.common.android.extensions.showPopup
import debts.core.common.android.extensions.toFormattedCurrency
import debts.core.common.android.extensions.toSimpleDateString
import debts.core.common.android.extensions.visible
import net.thebix.debts.R
import java.util.Date

@SuppressLint("ViewConstructor")
class DebtorItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val itemClickCallback: DebtorsAdapter.ItemClickCallback,
) : ConstraintLayout(context, attrs, defStyleAttr),
    ItemRenderer<DebtorsItemViewModel.DebtorItemViewModel> {

    private val avatarView: ImageView
    private val nameView: TextView
    private val amountView: TextView
    private val dateView: TextView

    private var debtorId: Long = 0

    init {
        selfInflate(R.layout.home_debtors_item_layout)
        doInRuntime {
            applyLayoutParams()
            setPaddingTopResCompat(R.dimen.padding_16dp)
            setPaddingBottomResCompat(R.dimen.padding_16dp)
            setPaddingStartResCompat(R.dimen.padding_16dp)
            setSelectableItemBackground()
        }
        this.setOnClickListener {
            itemClickCallback.onItemClick(debtorId)
        }
        avatarView = findViewById(R.id.home_debtors_item_avatar)
        nameView = findViewById(R.id.home_debtors_item_name)
        amountView = findViewById(R.id.home_debtors_item_amount)
        dateView = findViewById(R.id.home_debtors_item_date)
        this.setOnLongClickListener {
            this.showPopup(
                R.menu.home_debtors_item_menu,
                PopupMenu.OnMenuItemClickListener { menuItem ->
                    when (menuItem?.itemId) {
                        R.id.home_debtors_item_menu_remove -> {
                            itemClickCallback.onDebtorRemove(debtorId)
                            true
                        }

                        R.id.home_debtors_item_menu_share -> {
                            itemClickCallback.onDebtorShare(debtorId)
                            true
                        }

                        else -> false
                    }
                }
            )
            true
        }
    }

    override fun render(data: DebtorsItemViewModel.DebtorItemViewModel) {
        with(data) {
            debtorId = id
            nameView.text = name
            amountView.text = resources.getString(
                R.string.home_debtors_item_amount,
                currency,
                amount.toFormattedCurrency()
            )
            dateView.visible = lastDate != Long.MIN_VALUE
            dateView.text = resources.getString(
                R.string.home_debtors_item_date,
                Date(lastDate).toSimpleDateString()
            )
            Glide.with(avatarView)
                .load(if (avatarUrl.isNotBlank()) avatarUrl else R.mipmap.ic_launcher)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .fallback(R.mipmap.ic_launcher)
                .apply(RequestOptions.circleCropTransform())
                .into(avatarView)
        }
    }
}
