package net.thebix.debts.models;

import android.content.Context;
import android.database.Cursor;
import java.util.ArrayList;

// Список долгов для карточки должника
public class DebtsList {
    // region Инкапсуляция свойств
    private ArrayList<DebtsListItem> _Items;
    //endregion

    // region Свойства
    public ArrayList<DebtsListItem> getItems(){
        if(_Items == null)
            _Items = new ArrayList<DebtsListItem>();
        return _Items;
    }
    // endregion

    // region Конструкторы
    public DebtsList(Cursor cursor, Context context){
        _Items = new ArrayList<DebtsListItem>();

        while(cursor.moveToNext()) {
            _Items.add(new DebtsListItem(cursor, context));
        }
    }
    // endregion
}
