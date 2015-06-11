package net.thebix.debts.models;

import android.content.Context;
import android.database.Cursor;

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
    public ContactsList(Cursor cursor, Context context){
        _Items = new ArrayList<ContactsListItem>();

        while(cursor.moveToNext()) {
            _Items.add(new ContactsListItem(cursor, context));
        }
    }
    // endregion
}
