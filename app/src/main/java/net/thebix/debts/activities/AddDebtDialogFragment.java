package net.thebix.debts.activities;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import net.thebix.debts.DBAdapter;
import net.thebix.debts.DBHelper;
import net.thebix.debts.Misc;
import net.thebix.debts.R;
import net.thebix.debts.SDAdapter;
import net.thebix.debts.Throws;
import net.thebix.debts.adapters.ContactsAutocompleteListAdapter;
import net.thebix.debts.enums.Constants;
import net.thebix.debts.models.ContactsList;
import net.thebix.debts.models.ContactsListItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class AddDebtDialogFragment
                extends DialogFragment {

    // region Интерфейсы
    // Событие обновления долгов/должников
    public interface IDebtsOnUpdate {
        void onDebtsUpdate();
    }
    // endregion

    // region Переменные
    static IDebtsOnUpdate mListener; //Подписчик на событие, что долги обновились
    Long mContactId;
    String mContactName; //Имя контактов. сохраняется при выборе в выпадающем списке. Позволяет отслеживать, что юзер начал менять имя должника и => mContactId больше не актуален
    HashMap<Integer, Boolean> mToggleConfirm = new HashMap<Integer, Boolean>(); //Хэшмап с элементами, которые должны быть изменены, чтобы кнопка Confirm стала активной
    // endregion

    // region Конструкторы
    public static AddDebtDialogFragment newInstance(Fragment sourceFragment) { //TODO: передача параметром идентификатора выбранного должника в списке (добавление долга с карточки должника)
        try {
            mListener = (IDebtsOnUpdate)sourceFragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(sourceFragment.toString() + " must implement IDebtsOnUpdate");
        }

        AddDebtDialogFragment fragment = new AddDebtDialogFragment();
//        Bundle args = new Bundle();
//        args.putLong(Constants.KEY_DEBITOR_ID, debitorId);
//        fragment.setArguments(args);
        return fragment;
    }

    public AddDebtDialogFragment() {
        // Required empty public constructor
    }
    // endregion

    // region События
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState); //INFO: а может и не надо

        //getDialog().setTitle(getString(R.string.title_dialog_fragment_add_debt));
        View v = inflater.inflate(R.layout.dialog_fragment_add_debt, null);
        v.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        v.findViewById(R.id.buttonConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDebt();
                dismiss();
            }
        });
        v.findViewById(R.id.radioButtonSubtract).setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v){
                toggleConfirm(R.id.radioButtonAdd, true);//INFO: используем только radioButtonAdd, т.к. должна быть выбрана одна из кнопок
            }
        });
        v.findViewById(R.id.radioButtonAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v){
                toggleConfirm(R.id.radioButtonAdd, true);
            }
        });
        ((EditText)v.findViewById(R.id.editTextAmount)).addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String amount = s.toString();
                if(amount.length() > 0) {
                    if (Double.parseDouble(amount) != 0) {
                        toggleConfirm(R.id.editTextAmount, true);
                        return;
                    }
                }
                toggleConfirm(R.id.editTextAmount, false);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        AutoCompleteTextView contactListTextView = (AutoCompleteTextView)v.findViewById(R.id.autoCompleteTextViewName);
        ContactsList contacts = ContactsList.newInstance(getActivity());
        ContactsAutocompleteListAdapter adapter =
                new ContactsAutocompleteListAdapter(getActivity(), R.layout.autocomplete_contact_item, contacts.getItems());
        contactListTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View arg1, int index,
                                    long arg3) {
                ContactsListItem item = (ContactsListItem) av.getItemAtPosition(index);
                if(item == null)
                    return;
                mContactId = item.getId();
                mContactName = item.getName();
                //mContactListTextView.setText(""+name+"<"+number+">");
            }
        });
        contactListTextView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String name = s.toString();
                if (name.length() > 0) {
                    toggleConfirm(R.id.autoCompleteTextViewName, true);
                    if(mContactName != name) //если часть символов удалить из выбранного имени, должен обнуляться и идентификатор, т.к. теперь дебитор не соответствует контакту
                       mContactId = (long)0;
                    return;
                }
                mContactId = (long)0;
                toggleConfirm(R.id.autoCompleteTextViewName, false);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        contactListTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //TODO: поиск по контактам по введенному имени, если mContactId == 0
                    //mContactId = item.getId();
                }
            }
        });
        contactListTextView.setAdapter(adapter);
        return v;
    }

//    // Метод onDismiss срабатывает, когда диалог закрывается
//    @Override
//    public void onDismiss(DialogInterface dialog) {
//        super.onDismiss(dialog);
//    }
//
//    // Метод onCancel срабатывает, когда диалог отменяют кнопкой Назад
//    @Override
//    public void onCancel(DialogInterface dialog) {
//        super.onCancel(dialog);
//    }

    // endregion

    // region Закрытые методы
// Функция добавления/удаления долга
    private void updateDebt() {
        RadioButton radioAdd = (RadioButton) getView().findViewById(R.id.radioButtonAdd);
        boolean isAdd = radioAdd.isChecked();

        long debitorId = getDebitorId();

        //После внесения долга клавиатура убирается
        EditText editTextAmount = (EditText)getView().findViewById(R.id.editTextAmount);
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextAmount.getWindowToken(), 0);

        if (editTextAmount.length() == 0) // Если нечего вносить, ниего не делаем
            return;

        double sum = Double.parseDouble(editTextAmount.getText().toString());
        if(sum == 0) // Если нечего вносить, ниего не делаем
            return;

        if (!isAdd)
            sum = sum * -1;

        DBAdapter db = new DBAdapter(getActivity());
        try {
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

            long id = db.insertDebt(debitorId, currentDate, sum);
            Throws.ifLongNullOrZeroOrLess(id,
                    getString(R.string.err_debit_not_added));
            SDAdapter.writeDebit(getActivity().getApplicationContext(), this.mContactId,
                    currentDate, sum);

            Toast.makeText(getActivity(), R.string.message_debit_added,
                    Toast.LENGTH_SHORT).show();

            editTextAmount.setText("");
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
            Misc.WriteLog(ex);
        } finally {
            db.close();
        }

        mListener.onDebtsUpdate(); // оповестить подписчика, что долги обновились
    }

    // Добавление должника по mContactId или получение уже добавленного
    private Long getDebitorId(){
        // Получим контакт запросом ко всем контактам
        DBAdapter db = new DBAdapter(getActivity());
        Long debitorId = (long)0;
        try {
            if(mContactId > 0){ //Из контактов
                debitorId = db.getDebitorIdByContactId(this.mContactId);
            } else {
                //TODO: поиск по имени добавленных без contactId
                if(debitorId > 0){ // Если должник уже заведен, но не связан с контактом => обновить данные по нему
                    //TODO: db.updateDebitor(debitorId, debName, debPhone, debEmail);
                }
            }

            if(debitorId == 0){ //Должник так и не найден, добавляем как нового
                if (mContactId > 0) // Внесение должника ассоциированного с контактом
                    debitorId = db.insertDebitor(mContactId);
                else {// Не из контактов
                    //TODO: сначала пробуем искать по имени в контактах. Возможно, юзер вбил но не выбрал в выпадающем списке
                    debitorId = (long) 0;//TODO: не из контактов db.insertDebitor(debName, debPhone, debEmail);
                }

                //Т.к. новый юзер внесен, надо обновить меню actionbar, чтобы там отображались кнопки
                getActivity().invalidateOptionsMenu();
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
            Misc.WriteLog(ex);
        } finally {
            db.close();
            return debitorId;
        }
    }

    // Активировать/деактивировать кнопку Confirm на основе состояния всех связанных элементов
    // id - идентификатор только что изменившегося элемента, enable -- его влияние на кнопку Confirm
    void toggleConfirm(int id, boolean enable){
        mToggleConfirm.put(id, enable);
        Button buttonConfirm = (Button)getView().findViewById(R.id.buttonConfirm);
        buttonConfirm.setEnabled(mToggleConfirm.containsKey(R.id.autoCompleteTextViewName) && mToggleConfirm.get(R.id.autoCompleteTextViewName)
            && mToggleConfirm.containsKey(R.id.radioButtonAdd) && mToggleConfirm.get(R.id.radioButtonAdd)
            && mToggleConfirm.containsKey(R.id.editTextAmount) && mToggleConfirm.get(R.id.editTextAmount));
    }
    // endregion
}
