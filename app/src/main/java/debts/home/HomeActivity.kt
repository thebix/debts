package debts.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding3.view.clicks
import debts.core.common.android.BaseActivity
import debts.core.common.android.buildconfig.BuildConfigData
import debts.core.common.android.extensions.getColorCompat
import debts.core.common.android.navigation.ActivityScreenContext
import debts.core.common.android.navigation.ScreenContextHolder
import debts.core.repository.SortType
import debts.feature.adddebt.AddOrEditDebtDialogHolder
import debts.feature.adddebt.DebtLayoutData
import debts.feature.contacts.adapter.ContactsItemViewModel
import debts.home.list.adapter.DebtsPagerAdapter
import debts.home.list.mvi.HomeIntention
import debts.home.list.mvi.HomeState
import debts.home.list.mvi.HomeViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import net.thebix.debts.R
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class HomeActivity : BaseActivity() {

    private companion object {

        const val READ_CONTACTS_FOR_ADD_DEBT_DIALOG_PERMISSION_CODE = 1
        const val READ_CONTACTS_SYNC_PERMISSION_CODE = 2
    }

    private val intentionSubject = PublishSubject.create<HomeIntention>()
    private val screenContextHolder: ScreenContextHolder by inject()
    private val buildConfigData: BuildConfigData by inject()
    private val viewModel: HomeViewModel by viewModel()
    private val addOrEditDebtDialogHolderCallbacks = object : AddOrEditDebtDialogHolder.AddOrEditDebtDialogHolderCallback {

        override fun onConfirm(data: DebtLayoutData) {
            handleAddOrEditDialogConfirmation(data)
        }
    }

    private var fabView: View? = null
    private lateinit var menu: Menu
    private lateinit var disposables: CompositeDisposable

    private var addOrEditDebtDialogHolder: AddOrEditDebtDialogHolder? = null
    private var contacts: List<ContactsItemViewModel> = emptyList()
    private var dontShowAddDebtDialog: Boolean = true

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

        addOrEditDebtDialogHolder = AddOrEditDebtDialogHolder(this, addOrEditDebtDialogHolderCallbacks)

        val toolbarView: Toolbar = findViewById(R.id.home_toolbar)
        setSupportActionBar(toolbarView)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbarView.title = getString(R.string.app_name)
        toolbarView.setBackgroundColor(applicationContext.getColorCompat(net.thebix.debts.core.resource.R.color.colorPrimary))

        fabView = findViewById(R.id.home_fab)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_debtors_menu, menu)
        this.menu = menu
        val menuSearch = menu.findItem(R.id.home_debtors_menu_search)
        val searchView = menuSearch.actionView as SearchView
        searchView.queryHint = applicationContext.getString(R.string.home_debtors_search_hint)
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
            ActivityScreenContext(
                activity = this,
                applicationId = buildConfigData.getApplicationId(),
            )
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
        super.onStop()
    }

    override fun onDestroy() {
        fabView = null
        addOrEditDebtDialogHolder = null
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_debtors_menu_sort_name -> {
                intentionSubject.onNext(HomeIntention.ToggleSortByName(sortType))
                return true
            }

            R.id.home_debtors_menu_sort_amount -> {
                intentionSubject.onNext(HomeIntention.ToggleSortByAmount(sortType))
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
    @Suppress("NestedBlockDepth")
    private fun render(state: HomeState) {
        Timber.d("State is: $state")
        with(state) {
            if (::menu.isInitialized) {
                sortType.get(this)?.let { sortType ->
                    this@HomeActivity.sortType = sortType
                    val sortName = menu.findItem(R.id.home_debtors_menu_sort_name)
                    val sortAmount = menu.findItem(R.id.home_debtors_menu_sort_amount)
                    sortAmount.setIcon(net.thebix.debts.core.resource.R.drawable.ic_arrow_drop_down)
                    sortName.setIcon(net.thebix.debts.core.resource.R.drawable.ic_arrow_drop_down)
                    when (sortType) {
                        SortType.AMOUNT_DESC -> sortAmount.setIcon(net.thebix.debts.core.resource.R.drawable.ic_clear)
                        SortType.AMOUNT_ASC -> sortAmount.setIcon(net.thebix.debts.core.resource.R.drawable.ic_arrow_drop_up)
                        SortType.NAME_DESC -> sortName.setIcon(net.thebix.debts.core.resource.R.drawable.ic_clear)
                        SortType.NAME_ASC -> sortName.setIcon(net.thebix.debts.core.resource.R.drawable.ic_arrow_drop_up)
                        else -> {
                            // no-op
                        }
                    }
                }
            }
            this@HomeActivity.contacts = contacts
            showAddDebtDialog.get(this)?.let {
                if (dontShowAddDebtDialog.not()) {
                    dontShowAddDebtDialog = true
                    showAddDebtDialog()
                }
            }
        }
    }

    private fun intentions() =
        Observable.merge(
            listOf(
                Observable.fromCallable {
                    HomeIntention.Init(
                        Manifest.permission.READ_CONTACTS,
                        READ_CONTACTS_SYNC_PERMISSION_CODE
                    )
                },
                intentionSubject,
                (fabView?.clicks() ?: Observable.empty<HomeIntention>())
                    .doOnNext { dontShowAddDebtDialog = false }
                    .map {
                        HomeIntention.OpenAddDebtDialog(
                            Manifest.permission.READ_CONTACTS,
                            HomeActivity.READ_CONTACTS_FOR_ADD_DEBT_DIALOG_PERMISSION_CODE
                        )
                    }
            )
        )

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            READ_CONTACTS_FOR_ADD_DEBT_DIALOG_PERMISSION_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intentionSubject
                    .onNext(
                        HomeIntention.OpenAddDebtDialog(
                            Manifest.permission.READ_CONTACTS,
                            READ_CONTACTS_FOR_ADD_DEBT_DIALOG_PERMISSION_CODE
                        )
                    )
            } else {
                showAddDebtDialog()
            }

            READ_CONTACTS_SYNC_PERMISSION_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intentionSubject.onNext(HomeIntention.SyncWithContacts)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showAddDebtDialog() {
        addOrEditDebtDialogHolder?.showAddDebt(contacts = contacts)
    }

    private fun handleAddOrEditDialogConfirmation(data: DebtLayoutData) {
        with(data) {
            if (this.name.isNotBlank() && this.amount != 0.0) {
                intentionSubject.onNext(
                    HomeIntention.AddDebt(
                        this.contactId,
                        this.name,
                        this.amount,
                        this.comment,
                        this.date
                    )
                )
            } else {
                Snackbar
                    .make(
                        fabView!!,
                        R.string.home_debtors_empty_debt_fields,
                        Snackbar.LENGTH_SHORT
                    )
                    .show()
            }
        }
    }
}
