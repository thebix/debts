package debts.home.list.adapter

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import debts.common.android.adapters.ItemRenderer
import debts.common.android.bindView
import okb.common.android.extension.applyLayoutParams
import okb.common.android.extension.doInRuntime
import okb.common.android.extension.selfInflate
import net.thebix.debts.R

class AddDebtSuggestionItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ItemRenderer<ContactsItemViewModel> {

    private val nameView by bindView<TextView>(R.id.home_debtors_add_debt_contact_name)
    private val avatarView by bindView<ImageView>(R.id.home_debtors_add_debt_contact_avatar)

    init {
        selfInflate(R.layout.home_debtors_add_debt_contact_item)
        doInRuntime {
            applyLayoutParams()
        }
    }

    override fun render(data: ContactsItemViewModel) {
        with(data) {
            nameView.text = name
            Glide.with(context)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_launcher)
                .fallback(R.drawable.ic_launcher)
                .into(avatarView)
        }
    }
}
