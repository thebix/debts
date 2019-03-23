package debts.home.list.adapter

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import debts.common.android.adapters.ItemRenderer
import debts.common.android.extensions.applyLayoutParams
import debts.common.android.extensions.doInRuntime
import debts.common.android.extensions.selfInflate
import net.thebix.debts.R

class AddDebtSuggestionItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ItemRenderer<ContactsItemViewModel> {

    private val nameView: TextView
    private val avatarView: ImageView

    init {
        selfInflate(R.layout.home_debtors_add_debt_contact_item)
        doInRuntime {
            applyLayoutParams()
        }
        nameView = findViewById(R.id.home_debtors_add_debt_contact_name)
        avatarView = findViewById(R.id.home_debtors_add_debt_contact_avatar)
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
