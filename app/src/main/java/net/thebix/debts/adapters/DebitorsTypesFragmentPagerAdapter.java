package net.thebix.debts.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import net.thebix.debts.R;
import net.thebix.debts.activities.DebitorsListFragment;
import net.thebix.debts.enums.Constants;
import net.thebix.debts.enums.DebitorsListTypes;
import java.util.HashMap;

// Класс с адаптером пейджера для SwipeViews
public class DebitorsTypesFragmentPagerAdapter extends
        FragmentStatePagerAdapter {

    // region Переменные
    FragmentActivity mActivity;
    HashMap<Integer,DebitorsListFragment> mFragments = new HashMap<Integer,DebitorsListFragment>();
    // endregion

    // region Конструкторы
    public static DebitorsTypesFragmentPagerAdapter newInstance(FragmentActivity activity) {
        DebitorsTypesFragmentPagerAdapter adapter = new DebitorsTypesFragmentPagerAdapter(activity.getSupportFragmentManager());
        adapter.setContext(activity);
        return adapter;
    }

    public DebitorsTypesFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    // endregion

    // region Открытые методы
    // Получение фрагмента по номеру позиции
    @Override
    // i -- тип листа (DebitorsListTypes.Debitors, DebitorsListTypes.Debitors)
    public Fragment getItem(int i) {
        if(mFragments.containsKey(i))
            return mFragments.get(i);

        DebitorsListFragment fragment = DebitorsListFragment.newInstance(i);
        mFragments.put(i, fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return Constants.DEBTS_TYPES_TABS_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case DebitorsListTypes.Debitors:
                return mActivity.getString(R.string.title_debts_list_fragment_debitors);
            case DebitorsListTypes.MyDebts:
                return mActivity.getString(R.string.title_debts_list_fragment_my_debts);
        }
        return "NO NAME " + (position + 1);
    }

    //Получение активного фрагмента
    public int getItemPosition(Object object){
        return POSITION_NONE;
    }

    public void setContext(FragmentActivity activity){
        mActivity = activity;
    }
    // endregion

}
