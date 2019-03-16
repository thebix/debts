package debts.home.details.adapter

import androidx.recyclerview.widget.DiffUtil
import debts.common.android.adapters.CommonDiffUtilCallback
import debts.common.android.adapters.DelegatedAdapter
import debts.common.android.adapters.TypedAdapterDelegate
import debts.common.android.adapters.ViewHolderRenderer
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DebtsAdapter : DelegatedAdapter() {

    companion object {
        const val TYPE_DEBT = 1
    }

    override var items: List<DebtsItemViewModel> = emptyList()

    init {
        addDelegate(TYPE_DEBT, TypedAdapterDelegate { parent ->
            val layout = DebtItemLayout(parent.context)
            ViewHolderRenderer(layout)
        })
    }

    override fun getItemViewType(position: Int) = when (items[0]) {
        is DebtsItemViewModel.DebtItemViewModel -> TYPE_DEBT
    }

    fun setItems(newItems: List<DebtsItemViewModel>): Completable =
        Single.fromCallable<DiffUtil.DiffResult> { DiffUtil.calculateDiff(CommonDiffUtilCallback(items, newItems)) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { result ->
                this.items = newItems
                result.dispatchUpdatesTo(this)
            }
            .ignoreElement()

    fun replaceAllItems(newItems: List<DebtsItemViewModel>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
