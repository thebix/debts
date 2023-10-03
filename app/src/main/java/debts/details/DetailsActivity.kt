package debts.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import debts.core.common.android.BaseActivity
import debts.core.common.exeptions.NotExistsException
import net.thebix.debts.R

class DetailsActivity : BaseActivity() {

    companion object {

        private const val KEY_DEBTOR_ID = "KEY_DEBTOR_ID"

        @JvmStatic
        fun createIntent(context: Context, debtorId: Long) = Intent(context, DetailsActivity::class.java)
            .apply {
                putExtra(KEY_DEBTOR_ID, debtorId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_activity)

        if (savedInstanceState == null) {
            val debtorId = intent?.extras?.getLong(KEY_DEBTOR_ID) ?: throw NotExistsException
            replaceFragment(DetailsFragment.createInstance(debtorId), R.id.details_root, false)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(0, net.thebix.debts.core.resource.R.anim.fade_out_activity)
    }
}
