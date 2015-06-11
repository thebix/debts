package net.thebix.debts.models;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;

// Список контактов листа автокомплит
public class ContactsList {
    // region Инкапсуляция свойств
    private ArrayList<ContactsListItem> _Items;
    //endregion

    // region Свойства
    public ArrayList<ContactsListItem> getItems(){
        if(_Items == null)
            _Items = new ArrayList<ContactsListItem>();
        return _Items;
    }
    // endregion

    // region Конструкторы
    public static ContactsList newInstance(Context context){
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        return new ContactsList(cursor, context, true);
    }
    public ContactsList(Cursor cursor, Context context, boolean closeCursor){
        _Items = new ArrayList<ContactsListItem>();

        while(cursor.moveToNext()) {
            _Items.add(new ContactsListItem(cursor, context));
        }
        if(closeCursor)
            cursor.close();
    }
    // endregion

    // region Окрытые методы

    // endregion
}
