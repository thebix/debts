/* Карточка должника (activity_debitor2) */
package net.thebix.debts.activities;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import net.thebix.debts.R;
import net.thebix.debts.enums.Constants;

public class DebitorActivity extends BasicFragmentActivity  {
    // region Переменные
    private int mDebitorsType; // Тип листа с долгами (должники/мои долги) на который надо вернуться
    // endregion

    // region События
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debitor);

        // Иконка приложения в ActionBar кликабельна
        ActionBar actionBar = getActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        this.mDebitorsType = bundle.getInt(Constants.KEY_DEBITOR_LIST_TYPE_ID, 0);

          //INFO: если экран в лендскейпе и планшет, не надо показывть эту активити.
//        if (mIsDualPane) {
//            // If the screen is now in landscape mode, we can show the
//            // dialog in-line with the list so we don't need this activity.
//            finish();
//            return;
//        }

        if (savedInstanceState == null) {
            long debitorId = bundle.getLong(Constants.KEY_DEBITOR_ID, 0);
            long contactId = bundle.getLong(Constants.KEY_CONTACT_ID, 0);
            DebitorFragment debitor = DebitorFragment.newInstance(debitorId, contactId, mDebitorsType);
            getSupportFragmentManager().beginTransaction().add(R.id.containerDebitor, debitor).commit();
        }
    }


    // region ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // По нажатии на кнопку возвращаем на главную Активити
                startMainActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // endregion
    // endregion

    // region Открытые методы
    // Вызов главной активити (MainActivity)
    public void startMainActivity() {
        Intent i = new Intent(this, net.thebix.debts.activities.MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Убрать все прошлые
        // активитис из стека
        i.putExtra(Constants.KEY_DEBITOR_LIST_TYPE_ID, mDebitorsType);
        startActivity(i);
    }
    // endregion
}
