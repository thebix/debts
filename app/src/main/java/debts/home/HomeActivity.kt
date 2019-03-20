package debts.home

import android.os.Bundle
import debts.common.android.BaseActivity
import debts.home.list.DebtorsFragment
import net.thebix.debts.R

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        if (savedInstanceState == null) {
            replaceFragment(DebtorsFragment(), R.id.home_root, false)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
