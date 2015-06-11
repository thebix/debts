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

import net.thebix.debts.Misc;
import net.thebix.debts.R;
import net.thebix.debts.models.ContactsListItem;
import net.thebix.debts.models.DebtsListItem;

import java.util.List;

// Мапинг полей списка на вью элементов листа
public class ContactsAutocompleteListAdapter extends ArrayAdapter<ContactsListItem> {
    // region Переменные
    int resource;
    // endregion

    // region Конструкторы
    //Initialize adapter
    public ContactsAutocompleteListAdapter(Context context, int resource, List<ContactsListItem> items) {
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
        ContactsListItem item = getItem(position);

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

        ImageView userPictureImageView =(ImageView)itemView.findViewById(R.id.imageViewConactPicture); //TODO: выводить картинку пользователя
        TextView сontactNameTextView =(TextView)itemView.findViewById(R.id.textViewContactName);
        //TextView сontactPhoneTextView =(TextView)itemView.findViewById(R.id.textViewContactPhone);

        //Картинка
        if(item.getUserPic() == null) {
            userPictureImageView.setImageResource(R.drawable.ic_contact_picture);
        } else {
            userPictureImageView.setImageBitmap(item.getUserPic());
        }

        сontactNameTextView.setText(item.getName());
        //сontactPhoneTextView.setText(item.hasPhone() ? item.getPhones().get(0) : ""); //TODO: выводить все телефоны

        return itemView;
    }
    // endregion
}