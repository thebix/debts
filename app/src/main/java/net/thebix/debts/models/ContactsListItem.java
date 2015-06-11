package net.thebix.debts.models;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.view.Gravity;

import net.thebix.debts.DBHelper;
import net.thebix.debts.Misc;
import net.thebix.debts.R;

import java.util.ArrayList;
import java.util.Date;

// Элемент списка листа контактов автокомплит
public class ContactsListItem {
    // region Инкапсуляция свойств
    private Long _Id;
    private String _Name;
    private Bitmap _UserPic;
//    private Boolean _HasPhone;
//    private ArrayList<String> _Phones;
    // endregion

    //region Свойства
    public Long getId() { return _Id; }
    public String getName() { return _Name; }
    public Bitmap getUserPic() { return _UserPic; }
//    public Boolean hasPhone() { return _HasPhone; }
//    public ArrayList<String> getPhones() { return _Phones; }
    // endregion

    // region Конструкторы
    public ContactsListItem(Cursor cursor, Context context){
        _Id = cursor.getLong(cursor
                .getColumnIndex(ContactsContract.Contacts._ID));
        _Name = cursor.getString(cursor
                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        _UserPic = Misc.getContactPhoto(context, _Id);
//        Integer hasPhone = cursor
//                .getInt(cursor
//                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
//        _HasPhone = hasPhone > 0;
//        _Phones = new ArrayList<String>();
//        if(_HasPhone){
//            Cursor phones = context.getContentResolver().query(
//                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                    null,
//                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ _Id,
//                    null, null);
//            while (phones.moveToNext()){
//                String phoneNumber = phones.getString(
//                        phones.getColumnIndex(
//                                ContactsContract.CommonDataKinds.Phone.NUMBER));
//                _Phones.add(phoneNumber);
//            }
//            phones.close();
//        }
    }
    // endregion

    // region Открытые методы
    @Override
    public String toString() {
        return _Name;
    }
    // endregion
}

//public class ContactPhoneList{
//    // region Инкапсуляция свойств
//    private ArrayList<ContactPhoneListItem> _Items;
//    //endregion
//
//    // region Свойства
//    public ArrayList<ContactsListItem> getItems(){
//        if(_Items == null)
//            _Items = new ArrayList<ContactsListItem>();
//        return _Items;
//    }
//    // endregion
//
//    // region Конструкторы
//    public static ContactsList newInstance(Context context){
//        Cursor cursor = context.getContentResolver().query(
//                ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
//        return  new ContactsList(cursor, context);
//    }
//    public ContactsList(Cursor cursor, Context context){
//        _Items = new ArrayList<ContactsListItem>();
//
//        while(cursor.moveToNext()) {
//            _Items.add(new ContactsListItem(cursor, context));
//        }
//    }
//    // endregion
//}
//
//public class ContactPhoneListItem{
//    private Phone
//}