package debts.common.android.adapters

import androidx.recyclerview.widget.DiffUtil
import debts.home.list.adapter.DebtorsItemViewModel

class CommonDiffUtilCallback(
    private val oldItems: List<Any>,
    private val newItems: List<Any>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        val sameClass = oldItem::class == newItem::class
        return if (!sameClass) false
        else {
            areContentsTheSame(oldItemPosition, newItemPosition)
            when (oldItem) {
                is DebtorsItemViewModel.DebtorItemViewModel -> oldItem.id == (newItem as DebtorsItemViewModel.DebtorItemViewModel).id
                else -> areContentsTheSame(oldItemPosition, newItemPosition)
            }
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }
}
