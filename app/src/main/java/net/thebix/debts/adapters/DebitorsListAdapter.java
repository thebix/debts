package net.thebix.debts.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import net.thebix.debts.R;
import net.thebix.debts.models.DebitorsListItem;
import java.util.List;

// Мапинг полей списка на вью элементов листа
public class DebitorsListAdapter extends ArrayAdapter<DebitorsListItem> {
    int resource;
    //Initialize adapter
    public DebitorsListAdapter(Context context, int resource, List<DebitorsListItem> items) {
        super(context, resource, items);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout itemView;
        DebitorsListItem item = getItem(position);

        //Inflate the view
        if(convertView==null)
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

        TextView sumTextView =(TextView)itemView.findViewById(R.id.textViewDebitorDebitSum);
        LinearLayout linearViewSub1 = (LinearLayout)itemView.findViewById(R.id.linearViewDebitorInfo);
        ImageView imageViewDebitorPhoto = (ImageView)linearViewSub1.findViewById(R.id.imageViewDebitorPhoto);

        LinearLayout linearViewSub2 = (LinearLayout)linearViewSub1.findViewById(R.id.linearViewDebitorInfoTexts);
        TextView textViewDebitorName = (TextView)linearViewSub2.findViewById(R.id.textViewDebitorName);
        TextView textViewUpdateDate = (TextView)linearViewSub2.findViewById(R.id.textViewUpdateDate);

        //Имя
        textViewDebitorName.setText(item.getName());
        textViewDebitorName.setTag(item.getId());

        //Сумма
        sumTextView.setText(item.getSumText());

        //Картинка
        if(item.getUserPic() == null) {
            imageViewDebitorPhoto.setImageResource(R.drawable.ic_contact_picture);
        } else {
            imageViewDebitorPhoto.setImageBitmap(item.getUserPic());
        }

        //Дата
        textViewUpdateDate.setText(item.getEventDateText());

        return itemView;
    }
}