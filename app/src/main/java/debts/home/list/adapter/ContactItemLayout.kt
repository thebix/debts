package debts.home.list.adapter

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import debts.core.common.android.adapters.ItemRenderer
import debts.core.common.android.extensions.applyLayoutParams
import debts.core.common.android.extensions.doInRuntime
import debts.core.common.android.extensions.selfInflate
import net.thebix.debts.R

class ContactItemLayout @JvmOverloads constructor(
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
                .load(if (avatarUrl.isNotBlank()) avatarUrl else R.mipmap.ic_launcher)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .fallback(R.mipmap.ic_launcher)
                .apply(RequestOptions.circleCropTransform())
                .into(avatarView)
        }
    }
}
