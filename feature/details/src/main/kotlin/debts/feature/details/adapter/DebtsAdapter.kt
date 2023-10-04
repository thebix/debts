package debts.feature.details.adapter

import debts.core.common.android.adapters.DelegatedAdapter
import debts.core.common.android.adapters.TypedAdapterDelegate
import debts.core.common.android.adapters.ViewHolderRenderer

class DebtsAdapter(
    historyItemCallback: DebtItemLayout.HistoryItemCallback,
) : DelegatedAdapter() {

    companion object {
        const val TYPE_DEBT = 1
    }

    override var items: List<DebtsItemViewModel> = emptyList()

    init {
        addDelegate(
            TYPE_DEBT,
            TypedAdapterDelegate { parent ->
                val layout = DebtItemLayout(
                    parent.context,
                    historyItemCallback = historyItemCallback
                )
                ViewHolderRenderer(layout)
            }
        )
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is DebtsItemViewModel.DebtItemViewModel -> TYPE_DEBT
    }

    fun replaceAllItems(newItems: List<DebtsItemViewModel>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
