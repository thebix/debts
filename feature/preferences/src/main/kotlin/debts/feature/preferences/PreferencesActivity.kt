package debts.feature.preferences

import android.content.Context
import android.content.Intent
import android.os.Bundle
import debts.core.common.android.BaseActivity
import net.thebix.debts.feature.preferences.R

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
}
