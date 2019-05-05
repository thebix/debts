package debts.home

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import debts.common.android.BaseActivity
import debts.home.list.adapter.DebtsPagerAdapter
import net.thebix.debts.R

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        val pager = findViewById<ViewPager>(R.id.home_pager)
        val tabsTitles = listOf<String>(
            this.getString(R.string.home_pager_tab_all),
            this.getString(R.string.home_pager_tab_debtors),
            this.getString(R.string.home_pager_tab_creditors)
        )
        pager.adapter = DebtsPagerAdapter(supportFragmentManager, tabsTitles)
        val tabs = findViewById<TabLayout>(R.id.home_pager_tabs)
        tabs.setupWithViewPager(pager)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
