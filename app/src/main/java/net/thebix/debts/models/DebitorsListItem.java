package net.thebix.debts.models;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;

import net.thebix.debts.DBAdapter;
import net.thebix.debts.DBHelper;
import net.thebix.debts.Misc;
import net.thebix.debts.R;

// Элемент списка должников для главной активити
public class DebitorsListItem {
    // region Инкапсуляция свойств
    private Long _Id;
    private Long _ContactId;
    private String _Name;
    private Date _EventDate;
    private String _EventDateText;
    private double _Sum;
    private Bitmap _UserPic;
    // endregion

    // region Свойства
    public Long getId() { return _Id; }
    public String getName() { return _Name; }
    public Bitmap getUserPic() { return _UserPic; }
    public double getSum() { return  _Sum; }
    public String getSumText() { if(_Sum >= 0) return  Misc.getSumText(_Sum, true); else return Misc.getSumText(-_Sum, true); }
    public Date getEventDate() {return _EventDate;}
    public String getEventDateText() {return _EventDateText;}
    // endregion

    // region Конструкторы
    public DebitorsListItem(Cursor cursor, Context context){
        final int columnIndexId = cursor
                .getColumnIndexOrThrow(DBHelper.DebitorsEntry._ID);
        final int columnIndexContactId = cursor
                .getColumnIndexOrThrow(DBHelper.DebitorsEntry.COL_CONTACT_ID);
        final int columnIndexSum = cursor
                .getColumnIndexOrThrow(DBHelper.DebitsEntry.COL_SUM);
        final int columnIndexDate = cursor
                .getColumnIndexOrThrow(DBHelper.DebitsEntry.COL_EVENTDATE);

        final String updateText = context.getString(R.string.text_updated);

        _Id = cursor.getLong(columnIndexId);
        _ContactId = cursor.getLong(columnIndexContactId);
        _EventDate = new Date(cursor.getLong(columnIndexDate));
        _UserPic = Misc.getContactPhoto(context, _ContactId);
        _Sum = cursor.getDouble(columnIndexSum);
        _EventDateText = updateText + " " + Misc.getDateTimeFormattedString(_EventDate, context);

        // Заполним имя пользователя
        if (_ContactId > 0) {
            _Name = Misc.getContactDisplayName(context, _ContactId);
        } else {
            DBAdapter dbread = new DBAdapter(context);
            try {

                Cursor c = dbread.getDebitorById(_Id);
                if (c.moveToFirst()) {
                        _Name = c.getString(c.getColumnIndexOrThrow(DBHelper.DebitorsEntry.COL_NAME));
                }
            } finally {
                dbread.close();
            }
        }


    }
    // endregion
}
