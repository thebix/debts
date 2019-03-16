package debts.home.list.adapter

import androidx.recyclerview.widget.DiffUtil
import debts.common.android.adapters.CommonDiffUtilCallback
import debts.common.android.adapters.DelegatedAdapter
import debts.common.android.adapters.TypedAdapterDelegate
import debts.common.android.adapters.ViewHolderRenderer
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DebtorsAdapter : DelegatedAdapter() {

    companion object {
        const val TYPE_DEBTOR = 1
    }

    override var items: List<DebtorsItemViewModel> = emptyList()

    init {
        addDelegate(TYPE_DEBTOR, TypedAdapterDelegate { parent ->
            val layout = DebtorItemLayout(parent.context)
            ViewHolderRenderer(layout)
        })
    }

    override fun getItemViewType(position: Int) = when (items[0]) {
        is DebtorsItemViewModel.DebtorItemViewModel -> TYPE_DEBTOR
    }

    fun setItems(newItems: List<DebtorsItemViewModel>): Completable =
        Single.fromCallable<DiffUtil.DiffResult> { DiffUtil.calculateDiff(CommonDiffUtilCallback(items, newItems)) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { result ->
                this.items = newItems
                result.dispatchUpdatesTo(this)
            }
            .ignoreElement()

    fun replaceAllItems(newItems: List<DebtorsItemViewModel>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
