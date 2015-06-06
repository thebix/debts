package net.thebix.debts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import net.thebix.debts.Misc;
import net.thebix.debts.R;
import net.thebix.debts.models.DebtsListItem;
import java.util.List;

// Мапинг полей списка на вью элементов листа
public class DebtsListAdapter extends ArrayAdapter<DebtsListItem> {
    // region Переменные
    int resource;
    // endregion

    // region Конструкторы
    //Initialize adapter
    public DebtsListAdapter(Context context, int resource, List<DebtsListItem> items) {
        super(context, resource, items);
        this.resource = resource;
    }
    // endregion

    // region Открытые методы
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        RelativeLayout itemView;
        //Get the current alert object
        DebtsListItem item = getItem(position);

        //Inflate the view
        if(convertView == null)
        {
            itemView = new RelativeLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)getContext().getSystemService(inflater);
            vi.inflate(resource, itemView, true);
        }
        else
        {
            itemView = (RelativeLayout) convertView;
        }

        TextView sumTextView =(TextView)itemView.findViewById(R.id.textViewDebitSum);
        TextView directionTextView =(TextView)itemView.findViewById(R.id.textViewDebitDirection);
        TextView eventDateTextView =(TextView)itemView.findViewById(R.id.textViewDebitEventDate);
        LinearLayout layout = (LinearLayout)itemView.findViewById(R.id.layoutActivityDebitorDebtsList);

        double sum = item.getSum();
        sumTextView.setText(Misc.getSumText(sum < 0 ? -sum : sum, false));
        directionTextView.setText(item.getSign());
        eventDateTextView.setText(item.getEventDateText());
        layout.setGravity(item.getAlign());

        return itemView;
    }
    // endregion
}