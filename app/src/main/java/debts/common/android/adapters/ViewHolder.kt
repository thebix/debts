package debts.common.android.adapters

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class ViewHolder<in Data>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(data: Data)
}

class ViewHolderRenderer<in Data, out Layout>(private val layout: Layout) : ViewHolder<Data>(layout)
        where  Layout : android.view.View, Layout : ItemRenderer<Data> {

    override fun bind(data: Data) {
        layout.render(data)
    }
}

// Because RecyclerView.ViewHolder is abstract
//class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
