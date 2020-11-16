package debts.preferences

import android.content.Context
import android.content.Intent
import android.os.Bundle
import debts.common.android.BaseActivity
import debts.preferences.main.MainSettingsFragment
import net.thebix.debts.R

class PreferencesActivity : BaseActivity() {

    companion object {

        @JvmStatic
        fun createIntent(context: Context) = Intent(context, PreferencesActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences_activity)

        if (savedInstanceState == null) {
            replaceFragment(MainSettingsFragment(), R.id.preferences_root, false)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
