package net.thebix.debts.models;

import android.content.Context;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import net.thebix.debts.enums.DebitorsListSortTypes;
import net.thebix.debts.enums.DebitorsListTypes;

// Список должников для главной активити
public class DebitorsList {
    // region Инкапсуляция свойств
    private ArrayList<DebitorsListItem> _Items;
    // endregion

    // region Свойства
    public ArrayList<DebitorsListItem> getItems(){
        if(_Items == null)
            _Items = new ArrayList<DebitorsListItem>();

        return _Items;
    }

    public int getSize(){
        return getItems().size();
    }
    // endregion

    // region Внутренние переменные
    final Comparator<DebitorsListItem> comparatorAlph = new Comparator<DebitorsListItem>() {
        @Override
        public int compare(DebitorsListItem item1, DebitorsListItem item2) {
            return item1.getName().compareTo(item2.getName());
        }
    };
    final Comparator<DebitorsListItem> comparatorAlphDesc = new Comparator<DebitorsListItem>() {
        @Override
        public int compare(DebitorsListItem item1, DebitorsListItem item2) {
            return item2.getName().compareTo(item1.getName());
        }
    };
    final Comparator<DebitorsListItem> comparatorAmount = new Comparator<DebitorsListItem>() {
        @Override
        public int compare(DebitorsListItem item1, DebitorsListItem item2) {
            if(item1.getSum() > item2.getSum())
                return 1;
            if(item1.getSum() < item2.getSum())
                return -1;
            return  0;
        }
    };
    final Comparator<DebitorsListItem> comparatorAmountDesc = new Comparator<DebitorsListItem>() {
        @Override
        public int compare(DebitorsListItem item1, DebitorsListItem item2) {
            if(item2.getSum() > item1.getSum())
                return 1;
            if(item2.getSum() < item1.getSum())
                return -1;
            return  0;
        }
    };
    final Comparator<DebitorsListItem> comparatorDate = new Comparator<DebitorsListItem>() {
        @Override
        public int compare(DebitorsListItem item1, DebitorsListItem item2) {
            return item1.getEventDate().compareTo(item2.getEventDate());
        }
    };
    final Comparator<DebitorsListItem> comparatorDateDesc = new Comparator<DebitorsListItem>() {
        @Override
        public int compare(DebitorsListItem item1, DebitorsListItem item2) {
            return item2.getEventDate().compareTo(item1.getEventDate());
        }
    };
    // endregion

    // region Конструкторы
    public DebitorsList(){
        _Items = new ArrayList<DebitorsListItem>();
    }

    public DebitorsList(Cursor cursor, Context context, int sort, int listType){
        _Items = new ArrayList<DebitorsListItem>();
        if(cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                _Items.add(new DebitorsListItem(cursor, context));
            }

            if (sort != DebitorsListSortTypes.None)
                this.sort(sort, listType);
        }
    }
    // endregion

    // region Открытые методы
    public void sort(int sort, int listType){
        switch (sort){
            case DebitorsListSortTypes.Alphabetically:
                Collections.sort(_Items, comparatorAlph);
                break;
            case DebitorsListSortTypes.AlphabeticallyDesc:
                Collections.sort(_Items, comparatorAlphDesc);
                break;
            case DebitorsListSortTypes.Amount:
                if(listType == DebitorsListTypes.Debitors)
                    Collections.sort(_Items, comparatorAmount);
                else
                    Collections.sort(_Items, comparatorAmountDesc);
                break;
            case DebitorsListSortTypes.AmountDesc:
                if(listType == DebitorsListTypes.Debitors)
                    Collections.sort(_Items, comparatorAmountDesc);
                else
                    Collections.sort(_Items, comparatorAmount);
                break;
            case DebitorsListSortTypes.Date:
                Collections.sort(_Items, comparatorDate);
                break;
            case DebitorsListSortTypes.DateDesc:
                Collections.sort(_Items, comparatorDateDesc);
                break;
            default:
                break;
        }
    }
    // endregion


}
