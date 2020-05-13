package debts.home.list.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import debts.common.android.adapters.ViewHolder
import debts.common.android.adapters.ViewHolderRenderer

class ContactsAdapter(
    context: Context,
    private val items: List<ContactsItemViewModel>
) : ArrayAdapter<ContactsItemViewModel>(
    context, android.R.layout.simple_dropdown_item_1line, items
) {

    private var currentItems = items

    override fun getItem(position: Int): ContactsItemViewModel? {
        return currentItems[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: 0L
    }

    override fun getCount(): Int {
        return currentItems.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position) ?: return View(context)

        @Suppress("UNCHECKED_CAST")
        val convertViewRes = if (convertView == null) {
            val view = ContactItemLayout(context)
            val holder = ViewHolderRenderer(view)
            view.tag = holder
            view
        } else {
            convertView
        }
        @Suppress("UNCHECKED_CAST")
        (convertViewRes.tag as ViewHolder<ContactsItemViewModel>).bind(item)
        return convertViewRes
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults()
                    .apply {
                        val filteredItems =
                            if (constraint == null) items else items.filter { (_, name) ->
                                name.contains(constraint, true)
                            }
                        values = filteredItems
                        count = filteredItems.size
                    }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    @Suppress("UNCHECKED_CAST")
                    currentItems = results.values as List<ContactsItemViewModel>
                    notifyDataSetChanged()
                } else notifyDataSetInvalidated()
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as ContactsItemViewModel).name
            }
        }
    }
}
