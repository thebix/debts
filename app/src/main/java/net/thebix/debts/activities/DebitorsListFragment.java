/* Список должников (fragment_debts_list) */
package net.thebix.debts.activities;

import net.thebix.debts.DBAdapter;
import net.thebix.debts.Misc;
import net.thebix.debts.R;
import net.thebix.debts.Throws;
import net.thebix.debts.enums.DebitorsListTypes;
import net.thebix.debts.enums.DialogTopicTypes;
import net.thebix.debts.models.DebitorsList;
import net.thebix.debts.models.DebitorsListItem;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import net.thebix.debts.enums.Constants;
import net.thebix.debts.adapters.DebitorsListAdapter;

public class DebitorsListFragment extends Fragment implements
        ConfirmationDialogFragment.ConfirmationDialogListener {

    // region Переменные
	private int mDebitorsType; // Тип листа с долгами (должники/мои долги)
    boolean mDualPane;  //лэндскейп режим, широкий экран => отображается два фрагмента
    int mSelectedListPositon = 0; //Номер выбранной позиции листа
    private DebitorsListFragment mFragment; //Ссылка на текущий фграмент (this)
    // endregion

    // region Конструкторы
    public static DebitorsListFragment newInstance(int debitorsType) {
        DebitorsListFragment fragment = new DebitorsListFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.KEY_DEBITOR_LIST_TYPE_ID, debitorsType);
        fragment.setArguments(args);
        return fragment;
    }
    // endregion

    // region События
	// После создания тип листа присваивается из Bundle
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDebitorsType = getArguments() != null ? getArguments().getInt(
                Constants.KEY_DEBITOR_LIST_TYPE_ID) : DebitorsListTypes.Debitors;
        mFragment = this;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debitors_list, container, false);
        view.setTag("DebitorsListFragment_" + mDebitorsType);
        return  view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        showDebitors(); // Отображаем лист должников/долгов с суммарными долгами
        // Check to see if we have a frame in which to embed the details
        // fragment directly in the containing UI.
        View debitorFrame = getActivity().findViewById(R.id.containerDebitor);
        mDualPane = debitorFrame != null && debitorFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null) {
            //TODO: брать сохраненного на onPause выбранного mSelectedListPositon, mDebitorsType ///mDebitorId и выбранный лист mDebitorsType, mSelectedListPositon
        }

        if (mDualPane) {
            ListView listView = (ListView) getView().findViewById(
                    R.id.listViewDebitors);
            // In dual-pane mode, the list view highlights the selected item.
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Make sure our UI is in the correct state.
            showDebitor(mSelectedListPositon, mDebitorsType);
        }
	}

	@Override
	public void onResume() { // After a pause OR at startup
		super.onResume();

	}

    // Реализация интерфейса модального окна ConfirmationDialogListener
    //Ок в окне предупреждения
    public void onConfirmationDialogPositiveClick(DialogFragment dialog,
                                                  int topicId, long itemId) {
        DBAdapter db = new DBAdapter(getActivity());
        try {
            switch (topicId) {
                case DialogTopicTypes.RemoveOne: // Удаление одного долга контакта
                {
                    Throws.ifLongNullOrZeroOrLess(itemId,
                            getString(R.string.err_id_not_valid));
                    db.removeAllDebitsByDebitorId(itemId);
                    if (db.removeDebitorById(itemId)) {
                        Toast.makeText(getActivity(), R.string.message_debitor_removed,
                                Toast.LENGTH_SHORT).show();

                        //Обновим таб
                        DebitorsTypesFragment typesFragment = (DebitorsTypesFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.fragDebtsTypes);
                        typesFragment.updateTabs();
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
            Misc.WriteLog(ex);
        } finally {
            db.close();
        }
    }
    // endregion

    // region Закрытые методы
	// Отображение списка должников и общей суммы долга
	protected void showDebitors() {
        TextView textViewNoDebitors = (TextView) getView().findViewById(
                R.id.textViewNoDebitors);
        ListView listView = (ListView) getView().findViewById(
                R.id.listViewDebitors);
        TextView textViewDebitsSum = (TextView) getView().findViewById(
                R.id.textViewDebitsSummary);
        // Лист заполняется должниками либо долгами, в зависимости от типа
        // фрагмента
        DebitorsList debtorsList = getDebitorsList();
        // Настройки адаптера
        if(debtorsList.getSize() > 0){
                DebitorsListAdapter adapterArray = new DebitorsListAdapter(getActivity(),
                        R.layout.fragment_debitors_list_item,
                        debtorsList.getItems());
            // Обработчик клика по элементу листа (переход в Карточку
            // должника)
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    showDebitor(position, mDebitorsType);
                }
            });

            listView.setAdapter(adapterArray);
            setListViewChoice(listView);
            listView.setVisibility(View.VISIBLE);
            double d = Misc.getDebitsSum(debtorsList);
            if (d < 0)
                d = -d;
            textViewDebitsSum.setText(Misc.getSumText(d, true));
            textViewNoDebitors.setVisibility(View.GONE);

        } else // Нет должников
        {
            textViewNoDebitors.setText(mDebitorsType ==  DebitorsListTypes.Debitors ?
                    R.string.text_no_debitors : R.string.text_no_my_debts);
            listView.setVisibility(View.GONE);
            listView.setAdapter(null);
            textViewNoDebitors.setVisibility(View.VISIBLE);
            textViewDebitsSum.setText("0");
        }

	}

    //Получение листа с должниками/кому должен
    private DebitorsList getDebitorsList() {
        Cursor contactDebits = null;
        DebitorsList res = null;
        DBAdapter db = new DBAdapter(getActivity());
        try {
            switch (mDebitorsType) {
                case DebitorsListTypes.Debitors:
                    contactDebits = db.getAllDebitorsWithDebitSums();
                    break;
                case DebitorsListTypes.MyDebts:
                    contactDebits = db.getAllMyDebtsWithDebitSums();
                    break;
            }
            res = new DebitorsList(contactDebits, getActivity(), DebitorsTypesFragment.mSortType, mDebitorsType);
        } catch (Exception ex) {
            res = new DebitorsList();
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG)
                    .show();
            Misc.WriteLog(ex);

        } finally {
            db.close();
        }
        return res;
    }

    // Удаление элемента листа по лонгтапу
    private void setListViewChoice(ListView listView) {
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int index, long arg3) {
                DebitorsList debtorsList = getDebitorsList();
                DebitorsListItem item = debtorsList.getItems().get(index);

                if(item != null) {
                    // Удалить запись о долге
                    DialogFragment dialogFragment = ConfirmationDialogFragment
                            .newInstance(mFragment,
                                    R.string.modal_text_delete_debitor,
                                    DialogTopicTypes.RemoveOne, item.getId());
                    dialogFragment.show(getActivity().getFragmentManager(), "dialog");
                }

                return true;

            }
        });
    }

    // Отображение должника (вызов новой активити или отображение фрагмента)
    private void showDebitor(int selectedListPositon, int debitorsType){
        mSelectedListPositon = selectedListPositon;
        mDebitorsType = debitorsType;
        ListView listView = (ListView) getView().findViewById(
                R.id.listViewDebitors);
        DebitorsListItem debitor = (DebitorsListItem)listView.getItemAtPosition(selectedListPositon);
        if(debitor == null)
            return;

        long debitorId = debitor.getId();

        if (mDualPane) {
            //INFO: правильное отображение фрагмента с должником
//            // We can display everything in-place with fragments, so update
//            // the list to highlight the selected item and show the data.
//            getListView().setItemChecked(index, true);
//
//            // Check what fragment is currently shown, replace if needed.
//            DetailsFragment details = (DetailsFragment)
//                    getFragmentManager().findFragmentById(R.id.details);
//            if (details == null || details.getShownIndex() != index) {
//                // Make new fragment to show this selection.
//                details = DetailsFragment.newInstance(index);
//
//                // Execute a transaction, replacing any existing fragment
//                // with this one inside the frame.
//                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                if (index == 0) {
//                    ft.replace(R.id.details, details);
//                } else {
//                    ft.replace(R.id.a_item, details);
//                }
//                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//                ft.commit();
//            }

        } else {
            // Otherwise we need to launch a new activity to display
            // the dialog fragment with selected text.
            Intent i = new Intent(getActivity(), net.thebix.debts.activities.DebitorActivity.class);
            i.putExtra(Constants.KEY_DEBITOR_ID, debitorId);
            i.putExtra(Constants.KEY_DEBITOR_LIST_TYPE_ID, mDebitorsType);
            startActivity(i);
        }
    }
    // endregion
}
