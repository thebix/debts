/*  Главная активити (activity_main2) */
package net.thebix.debts.activities;

import android.os.Bundle;
import net.thebix.debts.R;

public class MainActivity extends BasicFragmentActivity {
    // region Cобытия
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    // endregion
}
