package net.thebix.debts.models;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.Gravity;

import net.thebix.debts.DBAdapter;
import net.thebix.debts.DBHelper;
import net.thebix.debts.Misc;
import net.thebix.debts.R;

import java.util.Date;

// Элемент списка долгов для карточки должника
public class DebtsListItem {
    // region Инкапсуляция свойств
    private Long _Id;
    private Date _EventDate;
    private String _EventDateText;
    private double _Sum;
    private String _Sign;
    private int _Align;
    // endregion

    //region Свойства
    public Long getId() { return _Id; }
    public double getSum() { return  _Sum; }
    public Date getEventDate() {return _EventDate;}
    public String getEventDateText() {return _EventDateText;}
    public String getSign() { return _Sign;}
    public int getAlign() { return _Align;}
    // endregion

    // region Конструкторы
    public DebtsListItem(Cursor cursor, Context context){
        final int columnIndexId = cursor
                .getColumnIndexOrThrow(DBHelper.DebitorsEntry._ID);
        final int columnIndexSum = cursor
                .getColumnIndexOrThrow(DBHelper.DebitsEntry.COL_SUM);
        final int columnIndexDate = cursor
                .getColumnIndexOrThrow(DBHelper.DebitsEntry.COL_EVENTDATE);

        _Id = cursor.getLong(columnIndexId);
        _EventDate = new Date(cursor.getLong(columnIndexDate));
        _EventDateText = Misc.getDateFormattedString(_EventDate, context);
        _Sum = cursor.getDouble(columnIndexSum);
        if(_Sum < 0){
            _Sign = context.getString(R.string.text_i_took_money);
            _Align = Gravity.END;
        } else {
            _Sign = context.getString(R.string.text_i_gave_money);
            _Align = Gravity.START;
        }
    }
    // endregion
}
