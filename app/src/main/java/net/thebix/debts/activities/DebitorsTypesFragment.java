/* Двигающийся список с типами должников (fragmetn_debts_types)*/
package net.thebix.debts.activities;

import android.app.Activity;
//import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import net.thebix.debts.*;
import net.thebix.debts.enums.Constants;
import net.thebix.debts.enums.DebitorsListSortTypes;
import net.thebix.debts.adapters.DebitorsTypesFragmentPagerAdapter;

public class DebitorsTypesFragment extends Fragment {
    // region Переменные
    DebitorsTypesFragmentPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    boolean mDualPane;  //лэндскейп режим, широкий экран => отображается два фрагмента
    public static int mSortType;  //Тип сортировки листа с долгами должники/мои долги)
    // endregion

    // region Конструкторы
//    public static DebitorsTypesFragment newInstance(String param1, String param2) {
//        DebitorsTypesFragment fragment = new DebitorsTypesFragment();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
//        return fragment;
//    }
    public DebitorsTypesFragment() {
        // Required empty public constructor
    }
    // endregion

    // region События
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Создается пейджер-адаптер, который возвращает нужный фрагмент
        mSectionsPagerAdapter =  DebitorsTypesFragmentPagerAdapter.newInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_debtors_types, container, false);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewPager = (ViewPager) getView().findViewById(R.id.viewPagerMainActivity);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //Установка того листа активным, который был до перехода на другую активити
        int debitorsType = getActivity().getIntent().getIntExtra(Constants.KEY_DEBITOR_LIST_TYPE_ID, 0);
        mViewPager.setCurrentItem(debitorsType);

        View debitorFrame = getActivity().findViewById(R.id.containerDebitor);
        mDualPane = debitorFrame != null && debitorFrame.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // region ActionBar меню
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    // Тут меняются названия кнопок сортировки, в зависимости от выбранного типа
    @Override
    public void onPrepareOptionsMenu (Menu menu){
        MenuItem sortMain = menu.findItem(R.id.menu_sort);
        SubMenu subMenu = sortMain.getSubMenu();
        MenuItem itemName = subMenu.findItem(R.id.menu_sort_alph);
        MenuItem itemDate = subMenu.findItem(R.id.menu_sort_date);
        MenuItem itemSum = subMenu.findItem(R.id.menu_sort_amount);
        String descending = getString(R.string.menu_sort_desc);
        String clear = getString(R.string.menu_sort_reset);

        itemName.setTitle(getString(R.string.menu_sort_alph));
        itemDate.setTitle(getString(R.string.menu_sort_date));
        itemSum.setTitle(getString(R.string.menu_sort_amount));

        switch (mSortType){
            case DebitorsListSortTypes.Alphabetically:
                itemName.setTitle(getString(R.string.menu_sort_alph) + " " + descending);
                break;
            case DebitorsListSortTypes.AlphabeticallyDesc:
                itemName.setTitle(getString(R.string.menu_sort_alph) + " " + clear);
                break;
            case DebitorsListSortTypes.Date:
                itemDate.setTitle(getString(R.string.menu_sort_date) + " " + descending);
                break;
            case DebitorsListSortTypes.DateDesc:
                itemDate.setTitle(getString(R.string.menu_sort_date) + " " + clear);
                break;
            case DebitorsListSortTypes.Amount:
                itemSum.setTitle(getString(R.string.menu_sort_amount) + " " + descending);
                break;
            case DebitorsListSortTypes.AmountDesc:
                itemSum.setTitle(getString(R.string.menu_sort_amount) + " " + clear);
                break;
        }
        super.onPrepareOptionsMenu(menu);
    }

    //  Клик по иконке ActionBar. Часть общего функционала находится в BasicFragmentActivity.java
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_new:
//                // добавить новый контакт-должник‚ onActivityResult
//                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
//                        ContactsContract.Contacts.CONTENT_URI);
//                startActivityForResult(contactPickerIntent, Constants.CONTACT_PICKER_RESULT);
                AddDebtDialogFragment dialog = AddDebtDialogFragment.newInstance();
                dialog.show(getFragmentManager(), null);
                break;
            case R.id.menu_add_new_no_contact:
                startDebitorActivity(-1);
                break;
            case R.id.menu_sort_alph:
                setCurrentSortType(DebitorsListSortTypes.Alphabetically, item);
                mSectionsPagerAdapter.notifyDataSetChanged(); //Работает в связке getItemPosition
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.menu_sort_date:
                setCurrentSortType(DebitorsListSortTypes.Date, item);
                mSectionsPagerAdapter.notifyDataSetChanged(); //Работает в связке getItemPosition
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.menu_sort_amount:
                setCurrentSortType(DebitorsListSortTypes.Amount, item);
                mSectionsPagerAdapter.notifyDataSetChanged(); //Работает в связке getItemPosition
                getActivity().invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // endregion

    // Получение данных из Активити (контакт пикер)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.CONTACT_PICKER_RESULT:
                    long contactId = Long.valueOf(data.getData()
                            .getLastPathSegment());
                    startDebitorActivity(contactId);
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    // endregion

    // region Открытые методы
    public void updateTabs()
    {
        if(mSectionsPagerAdapter != null)
            mSectionsPagerAdapter.notifyDataSetChanged();
    }
    // endregion

    // region Скрытые методы
    // Вызов активити Карточка должника (DebitorActivity)
    private void startDebitorActivity(long contacitId) {
        int currentTabPos = mViewPager.getCurrentItem();
        if (!mDualPane) {
            Intent intent = new Intent(getActivity(), net.thebix.debts.activities.DebitorActivity.class);
            if(contacitId > 0)
                intent.putExtra(Constants.KEY_CONTACT_ID, contacitId);
            intent.putExtra(Constants.KEY_DEBITOR_LIST_TYPE_ID, currentTabPos);
            startActivity(intent);
        } else {
            //INFO: сделать отображение фрагмента с должником в лендскейпе  http://developer.android.com/guide/components/fragments.html "void showDetails(int index)"
            // Передавать сontactId (разобраться, почему принимается debitorId, возможно это одно и тоже)
            // DisplayFragment (Fragment B) is in the layout (tablet layout),
            // so tell the fragment to update

        }
    }

    //Установка типа сортировки. С учетом, что повторное нажатие приводит к обратной сортировке, а следующее -- к ее отключению
    private void setCurrentSortType(int sort, MenuItem item){
        switch (sort){
            case DebitorsListSortTypes.Alphabetically:
                if(mSortType == DebitorsListSortTypes.Alphabetically) {
                    mSortType = DebitorsListSortTypes.AlphabeticallyDesc;
                    item.setTitle(R.string.menu_sort_alph + " " + R.string.menu_sort_desc);
                }
                else if(mSortType == DebitorsListSortTypes.AlphabeticallyDesc) {
                    mSortType = DebitorsListSortTypes.None;
                }
                else {
                    mSortType = DebitorsListSortTypes.Alphabetically;
                }
                break;
            case DebitorsListSortTypes.Amount:
                if(mSortType == DebitorsListSortTypes.Amount)
                    mSortType = DebitorsListSortTypes.AmountDesc;
                else if(mSortType == DebitorsListSortTypes.AmountDesc)
                    mSortType = DebitorsListSortTypes.None;
                else
                    mSortType = DebitorsListSortTypes.Amount;
                break;
            case DebitorsListSortTypes.Date:
                if(mSortType == DebitorsListSortTypes.Date)
                    mSortType = DebitorsListSortTypes.DateDesc;
                else if(mSortType == DebitorsListSortTypes.DateDesc)
                    mSortType = DebitorsListSortTypes.None;
                else
                    mSortType = DebitorsListSortTypes.Date;
                break;
        }
    }
    // endregion
}
