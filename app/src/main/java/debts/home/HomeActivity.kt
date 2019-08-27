package debts.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.UiThread
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import debts.common.android.ActivityScreenContext
import debts.common.android.BaseActivity
import debts.common.android.ScreenContextHolder
import debts.common.android.extensions.getColorCompat
import debts.home.list.adapter.DebtsPagerAdapter
import debts.home.list.mvi.HomeIntention
import debts.home.list.mvi.HomeState
import debts.home.list.mvi.HomeViewModel
import debts.repository.SortType
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import net.thebix.debts.R
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.viewModel
import timber.log.Timber

class HomeActivity : BaseActivity() {

    private val intentionSubject = PublishSubject.create<HomeIntention>()
    private val screenContextHolder: ScreenContextHolder by inject()
    private val viewModel: HomeViewModel by viewModel()

    private lateinit var menu: Menu
    private lateinit var disposables: CompositeDisposable

    private var sortType: SortType = SortType.NOTHING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        val pager = findViewById<ViewPager>(R.id.home_pager)
        val tabsTitles = listOf<String>(
            this.getString(R.string.home_pager_tab_all),
            this.getString(R.string.home_pager_tab_debtors),
            this.getString(R.string.home_pager_tab_creditors)
        )
        pager.apply {
            adapter = DebtsPagerAdapter(supportFragmentManager, tabsTitles)
            offscreenPageLimit = 2
        }
        val tabs = findViewById<TabLayout>(R.id.home_pager_tabs)
        tabs.setupWithViewPager(pager)

        val toolbarView: Toolbar = findViewById(R.id.home_toolbar)
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbarView.title = getString(R.string.app_name)
        toolbarView.setBackgroundColor(applicationContext.getColorCompat(R.color.colorPrimary) ?: 0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_debtors_menu, menu)
        this.menu = menu
        val menuSearch = menu.findItem(R.id.home_debtors_menu_search)
        val searchView = menuSearch.actionView as SearchView
        searchView.queryHint = applicationContext.getString(R.string.home_debtors_search_hint) ?: ""
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                intentionSubject.onNext(HomeIntention.Filter(newText))
                return true
            }
        })
        intentionSubject.onNext(HomeIntention.InitMenu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()

        screenContextHolder.set(
            ScreenContextHolder.ACTIVITY_HOME,
            ActivityScreenContext(this)
        )
        disposables = CompositeDisposable(
            viewModel.states()
                .subscribe(::render),
            viewModel.processIntentions(intentions())
        )
    }

    override fun onStop() {
        screenContextHolder.remove(ScreenContextHolder.ACTIVITY_HOME)
        disposables.dispose()
//        addDebtLayout = null
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_debtors_menu_sort_name -> {
                intentionSubject.onNext(HomeIntention.ToggleSortByName)
                return true
            }
            R.id.home_debtors_menu_sort_amount -> {
                intentionSubject.onNext(HomeIntention.ToggleSortByAmount)
                return true
            }
            R.id.home_debtors_menu_settings -> {
                intentionSubject.onNext(HomeIntention.OpenSettings)
                return true
            }
            R.id.home_debtors_menu_share -> {
                intentionSubject.onNext(
                    HomeIntention.ShareAllDebts(
                        applicationContext?.getString(R.string.home_debtors_share_title) ?: ""
                    )
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @UiThread
    private fun render(state: HomeState) {
        Timber.d("State is: $state")
        with(state) {
            if (this@HomeActivity.sortType != sortType) {
                this@HomeActivity.sortType = sortType
                val sortName = menu.findItem(R.id.home_debtors_menu_sort_name)
                val sortAmount = menu.findItem(R.id.home_debtors_menu_sort_amount)
                sortAmount.setIcon(R.drawable.ic_arrow_drop_down)
                sortName.setIcon(R.drawable.ic_arrow_drop_down)
                when (sortType) {
                    SortType.AMOUNT_DESC -> sortAmount.setIcon(R.drawable.ic_clear)
                    SortType.AMOUNT_ASC -> sortAmount.setIcon(R.drawable.ic_arrow_drop_up)
                    SortType.NAME_DESC -> sortName.setIcon(R.drawable.ic_clear)
                    SortType.NAME_ASC -> sortName.setIcon(R.drawable.ic_arrow_drop_up)
                    else -> {
                        // no-op
                    }
                }
            }
        }
    }

    private fun intentions() =
        Observable.merge(
            listOf(
                Observable.fromCallable {
                    HomeIntention.Init
//                    DebtorsIntention.Init(
//                        Manifest.permission.READ_CONTACTS,
//                        DebtorsFragment.READ_CONTACTS_SYNC_PERMISSION_CODE,
//                        when (page) {
//                            TabTypes.All.page -> TabTypes.All
//                            TabTypes.Debtors.page -> TabTypes.Debtors
//                            TabTypes.Creditors.page -> TabTypes.Creditors
//                            else -> TabTypes.All
//                        }
//                    )
                },
                intentionSubject
//                (fabView?.clicks() ?: Observable.empty<DebtorsIntention>())
//                    .doOnNext { dontShowAddDebtDialog = false }
//                    .map {
//                        DebtorsIntention.OpenAddDebtDialog(
//                            Manifest.permission.READ_CONTACTS,
//                            DebtorsFragment.READ_CONTACTS_FOR_ADD_DEBT_DIALOG_PERMISSION_CODE
//                        )
//                    }
            )
        )
}
