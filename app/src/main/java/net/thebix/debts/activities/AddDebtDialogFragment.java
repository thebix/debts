package net.thebix.debts.activities;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import net.thebix.debts.R;
import net.thebix.debts.adapters.ContactsAutocompleteListAdapter;
import net.thebix.debts.enums.Constants;
import net.thebix.debts.models.ContactsList;
import net.thebix.debts.models.ContactsListItem;

import java.util.ArrayList;

public class AddDebtDialogFragment
                extends DialogFragment {

    // region Переменные

    // endregion

    // region Конструкторы
    public static AddDebtDialogFragment newInstance() { //TODO: передача параметром идентификатора выбранного должника в списке
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
        v.findViewById(R.id.buttonOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: добавить долг addDebt(false);
                dismiss();
            }
        });
        AutoCompleteTextView contactListTextView = (AutoCompleteTextView)v.findViewById(R.id.autoCompleteTextViewName);

        ContactsList contacts = ContactsList.newInstance(getActivity());
        //ArrayList<String> contactsNames = new ArrayList<String>();
//        for(int i=0; i < contacts.getItems().size(); i++) {
//            contactsNames.add(contacts.getItems().get(i).getName());
//        }
//        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(), R.layout.autocomplete_contact_item, contactsNames);
//        contactListTextView.setAdapter(adapter1);

        ContactsAutocompleteListAdapter adapter =
                new ContactsAutocompleteListAdapter(getActivity(), R.layout.autocomplete_contact_item, contacts.getItems());

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

    // endregion
}
