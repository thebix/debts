package debts.home.list.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import debts.common.android.adapters.ItemRenderer
import debts.common.android.extensions.*
import java.util.*
import net.thebix.debts.R

@SuppressLint("ViewConstructor")
class DebtorItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val itemClickCallback: DebtorsAdapter.ItemClickCallback
) : ConstraintLayout(context, attrs, defStyleAttr),
    ItemRenderer<DebtorsItemViewModel> {

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
            this.showPopup(R.menu.home_debtors_item_menu, object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
                    return when (menuItem?.itemId) {
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
            })
            true
        }
    }

    override fun render(data: DebtorsItemViewModel) {
        if (data is DebtorsItemViewModel.DebtorItemViewModel) {
            with(data) {
                debtorId = id
                nameView.text = name
                amountView.text = resources.getString(
                    R.string.home_debtors_item_amount,
                    currency,
                    amount.toDecimal()
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
                } else {
                    avatarView.setImageResource(R.drawable.ic_launcher)
                }
            }
        }
    }
}
