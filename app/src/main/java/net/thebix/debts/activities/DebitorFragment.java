/* Фрагмент карточки должника (fragment_debitor) */
package net.thebix.debts.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import net.thebix.debts.DBAdapter;
import net.thebix.debts.DBHelper;
import net.thebix.debts.Misc;
import net.thebix.debts.R;
import net.thebix.debts.SDAdapter;
import net.thebix.debts.Throws;
import net.thebix.debts.adapters.DebtsListAdapter;
import net.thebix.debts.enums.Constants;
import net.thebix.debts.enums.DialogTopicTypes;
import net.thebix.debts.models.DebtsList;
import net.thebix.debts.models.DebtsListItem;
import java.math.BigDecimal;
import java.util.Date;
import android.view.View.OnClickListener;

public class DebitorFragment extends Fragment implements
        ConfirmationDialogFragment.ConfirmationDialogListener {

    // region Переменные
    private long mContactId;
    private long mDebitorId;
    private int mDebitorsType; // Тип листа с долгами (должники/мои долги) на который надо вернуться
    private DebitorFragment mFragment; //Ссылка на текущий фграмент (this)
    // endregion

    // region Конструкторы
    public static DebitorFragment newInstance(long debitorId, long contactId, int debitorsType) {
        DebitorFragment fragment = new DebitorFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_DEBITOR_ID, debitorId);
        args.putLong(Constants.KEY_CONTACT_ID, contactId);
        args.putInt(Constants.KEY_DEBITOR_LIST_TYPE_ID, debitorsType);
        fragment.setArguments(args);
        return fragment;
    }

    public DebitorFragment() {
        // Required empty public constructor
    }
    // endregion

    // region События
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = this;
        Bundle bundle = getArguments();
        if (bundle != null) {
            mDebitorId = bundle.getLong(Constants.KEY_DEBITOR_ID, 0);
            mDebitorsType = bundle.getInt(Constants.KEY_DEBITOR_LIST_TYPE_ID, 0);
            mContactId = bundle.getLong(Constants.KEY_CONTACT_ID, 0);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_debitor, container, false);
        final View view = inflater.inflate(R.layout.fragment_debitor, container, false);
        // Клик по кнопке добавления долга
        view.findViewById(R.id.buttonMinusDebit).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addDebt(false);
                    }
                }
        );
        view.findViewById(R.id.buttonPlusDebit).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addDebt(true);
                    }
                }
        );
        view.findViewById(R.id.buttonNoContactShowMoreFields).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onButtonNoContactShowMoreFields();
                    }
                }
        );
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Получим контакт запросом ко всем контактам
        DBAdapter db = new DBAdapter(getActivity());
        try {
            if (this.mDebitorId == 0) {
                // Если нет DebitorId, значит это может быть ContactId из приложения контактов
                if (this.mContactId > 0) // Добавление юзера из контактов
                {
                    this.mDebitorId = db.getDebitorIdByContactId(this.mContactId);
                }
            } else {
                // Есть DebitorId, значит должник внесен ранее
                Cursor cur = db.getDebitorById(this.mDebitorId);
                if (cur.moveToFirst()) {
                    this.mContactId = cur.getLong(cur
                            .getColumnIndex(DBHelper.DebitorsEntry.COL_CONTACT_ID));
                    // Если контакт из книги контактов не проассоциирован с mданным пользователем, заполняются дополнительные поля
                    if (this.mContactId <= 0) {
                        EditText editTextDebitorName = (EditText) getActivity().findViewById(R.id.editTextDebitorName);
                        editTextDebitorName.setText(cur.getString(cur
                                .getColumnIndex(DBHelper.DebitorsEntry.COL_NAME)));
                        EditText editTextNoContactDebitorPhone = (EditText) getActivity().findViewById(R.id.editTextNoContactDebitorPhone);
                        editTextNoContactDebitorPhone.setText(cur.getString(cur
                                .getColumnIndex(DBHelper.DebitorsEntry.COL_PHONE)));
                        EditText editTextNoContactDebitorEmail = (EditText) getActivity().findViewById(R.id.editTextNoContactDebitorEmail);
                        editTextNoContactDebitorEmail.setText(cur.getString(cur
                                .getColumnIndex(DBHelper.DebitorsEntry.COL_EMAIL)));
                    }
                }
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
            Misc.WriteLog(ex);
        } finally {
            db.close();
        }

        // Если есть связь с контактом, получение из контакта всей информации о человеке
        EditText editTextDebitorName = (EditText) getActivity().findViewById(R.id.editTextDebitorName);
        if (this.mContactId > 0) {
            editTextDebitorName.setText(Misc.getContactDisplayName(getActivity(),
                    this.mContactId));

            // Получим картинку пользователя и отобразим ее на экране
            ImageView contactPhoto = (ImageView) getActivity().findViewById(R.id.imageViewDebitorPhoto);
            Bitmap bm = Misc.getContactPhoto(getActivity(), this.mContactId);
            if (bm != null)
                contactPhoto.setImageBitmap(bm);

            editTextDebitorName.setTextColor(Resources.getSystem().getColor(
                    android.R.color.black));

        } else // Должник не связан с контактом
        {
            // Отображение дополнительных контролов
            ImageButton buttonNoContactShowMoreFields = (ImageButton) getActivity().findViewById(R.id.buttonNoContactShowMoreFields);
            buttonNoContactShowMoreFields.setVisibility(ImageButton.VISIBLE);
            editTextDebitorName.setEnabled(true);
        }

        // Обновляем таблицу долгов на этой странице, если должник существует
        if(this.mDebitorId > 0 || this.mContactId > 0)
        {
            EditText editTextAddDebit = (EditText)getActivity().findViewById(R.id.editTextAddDebit);
            editTextAddDebit.requestFocus(); //Если юзер есть, фоку на ввод долга
        }

        if (this.mDebitorId > 0)
        {
            initOrUpdateDebits();
        }
    }

    @Override
    public void onResume() { // After a pause OR at startup
        super.onResume();
        //TODO: Проверить, что заполняются поля после возврата (mDebitorsType, mDebitorId, mContactId)
        //mDebitorsType = getIntent().getIntExtra(Constants.KEY_DEBITOR_LIST_TYPE_ID, 0);
    }

    // region Action Bar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //INFO: возможно, в layout-mode это меню будет стирать меню главного фрагмента.
        //Тогда перечитать статью http://www.grokkingandroid.com/adding-action-items-from-within-fragments/
        //и сделать несколько лейаутов с меню (все варианты), либо одно меню и правильное скрытие кнопок
        inflater.inflate(R.menu.menu_debitor, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_remove_debitor: {
                // Удалить должника с его долгами
                DialogFragment dialogFragment = ConfirmationDialogFragment
                        .newInstance(mFragment, R.string.modal_text_delete_debitor,
                                DialogTopicTypes.RemoveAll, 0);
                dialogFragment.show(getActivity().getFragmentManager(), "dialog");
                break;
            }
            case R.id.menu_remove_debitor_history: {
                // Удалить должника с его долгами
                DialogFragment dialogFragment = ConfirmationDialogFragment
                        .newInstance(mFragment,
                                R.string.modal_text_delete_debitor_history,
                                DialogTopicTypes.RemoveHistory, 0);
                dialogFragment.show(getActivity().getFragmentManager(), "dialog");
                break;
            }
            default:
        }
        return super.onOptionsItemSelected(item);
    }
    // endregion

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    //Ок в окне предупреждения
    public void onConfirmationDialogPositiveClick(DialogFragment dialog,
                                                  int topicId, long itemId) {
        DBAdapter db = new DBAdapter(getActivity());
        try {
            switch (topicId) {
                case DialogTopicTypes.RemoveAll: // Удаление всех долгов дебитора
                {
                    db.removeAllDebitsByDebitorId(this.mDebitorId);
                    if (db.removeDebitorById(this.mDebitorId)) {
                        showMain();
                        Toast.makeText(getActivity(), R.string.message_debitor_removed,
                                Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getActivity(), R.string.err_remove_fail,
                                Toast.LENGTH_LONG).show();
                    break;
                }
                case DialogTopicTypes.RemoveOne: // Удаление одного долга контакта
                {
                    Throws.ifLongNullOrZeroOrLess(itemId,
                            getString(R.string.err_id_not_valid));
                    if (db.removeDebitById(itemId)) {
                        Toast.makeText(getActivity(), R.string.message_debit_removed,
                                Toast.LENGTH_SHORT).show();
                        initOrUpdateDebits();
                    }
                    break;
                }
                case DialogTopicTypes.RemoveHistory: // Удаление только истории
                    // контакта
                {
                    if (db.removeAllDebitsByDebitorId(this.mDebitorId)) {
                        showMain();
                        Toast.makeText(getActivity(),
                                R.string.message_debitor_history_removed,
                                Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getActivity(), R.string.err_remove_fail,
                                Toast.LENGTH_LONG).show();
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
    // Инициализация и отображение списка долгов данного пользователя
    public void initOrUpdateDebits() {
        toggleAdditionalFields(true);

        DBAdapter db = new DBAdapter(getActivity());
        try {
            Cursor debits = db.getAllDebitsByDebitorId(this.mDebitorId);

            ListView listView = (ListView) getActivity().findViewById(R.id.listViewDebits);
            TextView textViewDebitsSum = (TextView) getActivity().findViewById(R.id.textViewDebitsSum);
            TextView textViewDebitsHistoryTitle = (TextView) getActivity().findViewById(R.id.textViewDebitsHistoryTitle);

            if (debits.getCount() > 0) {
                DebtsList debtsList = new DebtsList(debits, getActivity());
                DebtsListAdapter adapterArray =  new DebtsListAdapter(getActivity(),
                        R.layout.fragment_debitor_debts_list_item,
                        debtsList.getItems());

                setListViewChoice(listView); //TODO: установить обработчики тапа и лонг-тапа для элементов листа в отдельном методе (аналог  посмотреть в DebitorsListFragment)
                listView.setAdapter(adapterArray);
                listView.setVisibility(View.VISIBLE);
                textViewDebitsHistoryTitle.setVisibility(TextView.VISIBLE);

                double d = Misc.getDebitsSum(debits);
                TextView textViewDebitDirection = (TextView) getActivity().findViewById(R.id.textViewDebitDirection);
                if (d == 0) {
                    textViewDebitDirection.setText(R.string.text_no_debts);
                } else if (d < 0) {
                    textViewDebitDirection.setText(R.string.text_i_took_money);
                    d = -d;
                } else if (d > 0) {
                    textViewDebitDirection.setText(R.string.text_i_gave_money);
                }

                textViewDebitsSum.setText(Misc.getSumText(d, true));
            } else {
                textViewDebitsHistoryTitle.setVisibility(TextView.GONE);
                listView.setAdapter(null);
                textViewDebitsSum.setText("0");
            }

        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
            Misc.WriteLog(ex);
        } finally {
            db.close();
        }
    }

    // Клик по кнопке отображения дополнительных полей для юзера не из контактов
    private void onButtonNoContactShowMoreFields() {
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.layoutNoContactDebitorAdditionalFields);
        toggleAdditionalFields(layout.getVisibility() == View.VISIBLE);
    }

    //Спрятать или показать дополнительные поля (для должника не из контактов)
    //hide=true => скрыть
    private void toggleAdditionalFields(Boolean hide){
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.layoutNoContactDebitorAdditionalFields);
        ImageButton buttonNoContactShowMoreFields = (ImageButton) getActivity().findViewById(R.id.buttonNoContactShowMoreFields);
        if(hide){
            layout.setVisibility(View.GONE);
            buttonNoContactShowMoreFields
                    .setImageResource(R.drawable.ic_navigation_expand);
        } else {
            buttonNoContactShowMoreFields
                    .setImageResource(R.drawable.ic_navigation_collapse);
            layout.setVisibility(View.VISIBLE);
        }

    }

    // Удаление элемента листа по лонгтапу
    private void setListViewChoice(ListView listView) {
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int index, long arg3) {
                ListView list = (ListView) getActivity().findViewById(R.id.listViewDebits);
                DebtsListAdapter adapter = (DebtsListAdapter)list.getAdapter();
                DebtsListItem item = adapter.getItem(index);
                Long id = item.getId();

				DialogFragment dialogFragment = ConfirmationDialogFragment
						.newInstance(mFragment,
                                R.string.modal_text_delete_debt,
                                DialogTopicTypes.RemoveOne, id);
				dialogFragment.show(getActivity().getFragmentManager(), "dialog");

                return true;
            }
        });
    }

    // Функция добавления/удаления долга
    private void addDebt(Boolean isPlusDebt) {
        EditText editTextAddDebit = (EditText) getActivity().findViewById(R.id.editTextAddDebit);

        //После внесения долга клавиатура убирается
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextAddDebit.getWindowToken(), 0);

        if (editTextAddDebit.length() == 0) // Если нечего вносить, ниего не делаем
            return;

        double sum = Double.parseDouble(editTextAddDebit.getText().toString());
        if(sum == 0) // Если нечего вносить, ниего не делаем
            return;

        if (!isPlusDebt)
            sum = sum * -1;

        DBAdapter db = new DBAdapter(getActivity());
        try {
            // Если должник не проассоциирован с контактом, надо обновить данные
            // с активити
            String debName = "";
            String debPhone = "";
            String debEmail = "";
            if (this.mContactId == 0) // Если не прикреплен контакт,
            // заводим/апдейтим без него => с
            // дополнительными полями
            {
                EditText editTextDebitorName = (EditText) getActivity().findViewById(R.id.editTextDebitorName);
                debName = editTextDebitorName.getText().toString();
                if (debName.length() == 0) // Если у должника нет имени
                {
                    Toast.makeText(getActivity(), R.string.err_no_debitor_name,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                EditText editTextNoContactDebitorPhone = (EditText) getActivity().findViewById(R.id.editTextNoContactDebitorPhone);
                debPhone = editTextNoContactDebitorPhone.getText().toString();
                EditText editTextNoContactDebitorEmail = (EditText) getActivity().findViewById(R.id.editTextNoContactDebitorEmail);
                debEmail = editTextNoContactDebitorEmail.getText().toString();

            }

            // Добавление/апдейт должника
            if (this.mDebitorId == 0) // Если должник не был заведен, сначала его
            // надо завести
            {
                if (this.mContactId > 0) // Внесение должника ассоциированного с
                    // контактом
                    this.mDebitorId = db.insertDebitor(this.mContactId);
                else
                    this.mDebitorId = db.insertDebitor(debName, debPhone,
                            debEmail);

                //Т.к. новый юзер внесен, надо обновить меню actionbar, чтобы там отображались кнопки
                getActivity().invalidateOptionsMenu();
            } else if (this.mContactId == 0) // Если должник уже заведен, но не
            // связан с контактом => обновить
            // данные по нему
            {
                db.updateDebitor(this.mDebitorId, debName, debPhone, debEmail);
            }

            // Записываем в БД новую строку с данными, связанную с этим юзером
            Date currentDate = new Date();

            double sumAbs = Math.abs(sum);
            if (sumAbs > Constants.MAX_DEBIT_VALUE) {
                Toast.makeText(getActivity(), R.string.err_value_to_long,
                        Toast.LENGTH_SHORT).show();
                return;
            } else if (sumAbs < Constants.MIN_DEBIT_VALUE)
            {
                Toast.makeText(getActivity(), R.string.err_value_to_small,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Приведем сумму к 2 числам после запятой
            sum = new BigDecimal(sum).setScale(2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();

            long id = db.insertDebt(this.mDebitorId, currentDate, sum);
            Throws.ifLongNullOrZeroOrLess(id,
                    getString(R.string.err_debit_not_added));
            SDAdapter.writeDebit(getActivity().getApplicationContext(), this.mContactId,
                    currentDate, sum);

            Toast.makeText(getActivity(), R.string.message_debit_added,
                    Toast.LENGTH_SHORT).show();

            editTextAddDebit.setText("");
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
            Misc.WriteLog(ex);
        } finally {
            db.close();
        }

        // Обновляем таблицу долгов на этой странице
        initOrUpdateDebits();
    }

    // Отображение главного окна
    private void showMain() {
        //TODO: если не планшет, код ниже
        DebitorActivity activity = (DebitorActivity)getActivity();
        activity.startMainActivity();
        //INFO: Если же планшет, продумать как возвращать, если это два фрагмента в лендскейпе. Скорее всего просто обновлять данный фрагмент
        //Посмотреть на реализацию startDebitorActivity в DebitorsTypesFrament
    }
    // endregion
}
