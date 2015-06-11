package net.thebix.debts.models;

import android.content.Context;
import android.database.Cursor;
import android.view.Gravity;

import net.thebix.debts.DBHelper;
import net.thebix.debts.Misc;
import net.thebix.debts.R;

import java.util.Date;

// Элемент списка листа контактов автокомплит
public class ContactsListItem {
    // region Инкапсуляция свойств
    private Long _Id;
    // endregion

    //region Свойства
    public Long getId() { return _Id; }
    // endregion

    // region Конструкторы
    public ContactsListItem(Cursor cursor, Context context){
        final int columnIndexId = cursor
                .getColumnIndexOrThrow(DBHelper.DebitorsEntry._ID);

        _Id = cursor.getLong(columnIndexId);
    }
    // endregion
}
