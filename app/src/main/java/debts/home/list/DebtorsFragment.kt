package debts.home.list

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.annotation.UiThread
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import debts.common.android.BaseActivity
import debts.common.android.BaseFragment
import debts.common.android.extensions.findViewById
import debts.common.android.extensions.getColorCompat
import debts.common.android.extensions.getDrawableCompat
import debts.common.android.extensions.showAlert
import debts.common.android.FragmentScreenContext
import debts.common.android.ScreenContextHolder
import debts.common.android.ScreenContextHolder.Companion.FRAGMENT_DEBTORS
import debts.home.details.mvi.DetailsResult
import debts.home.list.adapter.ContactsItemViewModel
import debts.home.list.adapter.DebtorsAdapter
import debts.home.list.mvi.DebtorsIntention
import debts.home.list.mvi.DebtorsState
import debts.home.list.mvi.DebtorsViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.koin.android.viewmodel.ext.viewModel
import timber.log.Timber
import org.koin.android.ext.android.inject
import net.thebix.debts.R

class DebtorsFragment : BaseFragment() {

    private companion object {

        const val KEY_RECYCLER_STATE = "KEY_RECYCLER_STATE"
        const val READ_CONTACTS_FOR_ADD_DEBT_DIALOG_PERMISSION_CODE = 1
        const val READ_CONTACTS_SYNC_PERMISSION_CODE = 2
    }

    private var toolbarView: Toolbar? = null
    private var recyclerView: RecyclerView? = null
    private var fabView: View? = null

    private val itemCallback = object : DebtorsAdapter.ItemClickCallback {
        override fun onItemClick(debtorId: Long) {
            recyclerState =
                recyclerView?.layoutManager?.onSaveInstanceState() as LinearLayoutManager.SavedState
            recyclerView?.adapter = null
            if (hasNameFilter) {
                intentionSubject.onNext(DebtorsIntention.Filter())
            }
            intentionSubject.onNext(DebtorsIntention.OpenDetails(debtorId, R.id.home_root))
        }

        override fun onDebtorRemove(debtorId: Long) {
            intentionSubject.onNext(DebtorsIntention.RemoveDebtor(debtorId))
        }

        override fun onDebtorShare(debtorId: Long) {
            intentionSubject.onNext(
                DebtorsIntention.ShareDebtor(
                    debtorId,
                    resources.getString(R.string.home_details_share_title),
                    resources.getString(R.string.home_details_share_message_borrowed),
                    resources.getString(R.string.home_details_share_message_lent)
                )
            )
        }

    }
    private val screenContextHolder: ScreenContextHolder by inject()
    private val viewModel: DebtorsViewModel by viewModel()
    private val adapter = DebtorsAdapter(itemCallback)
    private val intentionSubject = PublishSubject.create<DebtorsIntention>()

    private lateinit var disposables: CompositeDisposable
    private lateinit var menu: Menu
    private var adapterDisposable: Disposable? = null
    private var isConfigurationChange: Boolean = false
    private var addDebtLayout: AddDebtLayout? = null
    private var recyclerState: LinearLayoutManager.SavedState? = null
    private var sortType: DebtorsState.SortType = DebtorsState.SortType.NOTHING
    private var contacts: List<ContactsItemViewModel> = emptyList()
    private var dontShowAddDebtDialog: Boolean = true
    private var hasNameFilter = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isConfigurationChange = savedInstanceState != null

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        inflater.inflate(R.layout.home_debtors_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarView = findViewById(R.id.home_toolbar)
        recyclerView = findViewById(R.id.home_debtors_recycler)
        fabView = findViewById(R.id.home_debtors_fab)

        setHasOptionsMenu(true)
        (activity as BaseActivity).setSupportActionBar(toolbarView)
        (activity as BaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbarView?.title = getString(R.string.app_name)
        toolbarView?.setBackgroundColor(context?.getColorCompat(R.color.colorPrimary) ?: 0)

        recyclerView?.apply {
            adapter = this@DebtorsFragment.adapter

            addItemDecoration(
                DividerItemDecoration(
                    context.applicationContext,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(context.applicationContext.getDrawableCompat(R.drawable.list_divider_start_66dp))
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()

        screenContextHolder.set(
            FRAGMENT_DEBTORS,
            FragmentScreenContext(this)
        )
        disposables = CompositeDisposable(
            viewModel.states()
                .subscribe(::render),
            viewModel.processIntentions(intentions())
        )
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(
            KEY_RECYCLER_STATE,
            recyclerView?.layoutManager?.onSaveInstanceState()
        )
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getParcelable<LinearLayoutManager.SavedState>(KEY_RECYCLER_STATE)
            ?.let { state ->
                recyclerState = state
            }
        recyclerState
            ?.let { savedState ->
                recyclerView?.layoutManager?.onRestoreInstanceState(savedState)
            }

    }

    override fun onResume() {
        super.onResume()
        recyclerState?.let {
            recyclerView?.layoutManager?.onRestoreInstanceState(it)
        }
    }

    override fun onDestroyView() {
        recyclerView = null
        fabView = null
        toolbarView = null
        super.onDestroyView()
    }

    override fun onStop() {
        screenContextHolder.remove(FRAGMENT_DEBTORS)
        disposables.dispose()
        adapterDisposable?.dispose()
        addDebtLayout = null
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.home_debtors_menu, menu)
        this.menu = menu
        val menuSearch = menu.findItem(R.id.home_debtors_menu_search)
        val searchView = menuSearch.actionView as SearchView
        searchView.queryHint = context?.getString(R.string.home_debtors_search_hint) ?: ""
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                intentionSubject.onNext(DebtorsIntention.Filter(newText))
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_debtors_menu_sort_name -> {
                intentionSubject.onNext(DebtorsIntention.ToggleSortByName)
                return true
            }
            R.id.home_debtors_menu_sort_amount -> {
                intentionSubject.onNext(DebtorsIntention.ToggleSortByAmount)
                return true
            }
            R.id.home_debtors_menu_settings -> {
                intentionSubject.onNext(DebtorsIntention.OpenSettings)
                return true
            }
            R.id.home_debtors_menu_share -> {
                intentionSubject.onNext(
                    DebtorsIntention.ShareAllDebts(
                        context?.getString(R.string.home_debtors_share_title) ?: ""
                    )
                )
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @UiThread
    private fun render(state: DebtorsState) {
        Timber.d("State is: $state")
        with(state) {
            adapterDisposable?.dispose()
            if (isConfigurationChange) {
                adapter.replaceAllItems(filteredItems)
                isConfigurationChange = false
            } else {
                adapterDisposable = adapter.setItems(filteredItems)
                    .subscribe()
            }
            if (this@DebtorsFragment.sortType != sortType) {
                this@DebtorsFragment.sortType = sortType
                val sortName = menu.findItem(R.id.home_debtors_menu_sort_name)
                val sortAmount = menu.findItem(R.id.home_debtors_menu_sort_amount)
                sortAmount.setIcon(R.drawable.ic_arrow_drop_down)
                sortName.setIcon(R.drawable.ic_arrow_drop_down)
                when (sortType) {
                    DebtorsState.SortType.AMOUNT_DESC -> sortAmount.setIcon(R.drawable.ic_clear)
                    DebtorsState.SortType.AMOUNT_ASC -> sortAmount.setIcon(R.drawable.ic_arrow_drop_up)
                    DebtorsState.SortType.NAME_DESC -> sortName.setIcon(R.drawable.ic_clear)
                    DebtorsState.SortType.NAME_ASC -> sortName.setIcon(R.drawable.ic_arrow_drop_up)
                    else -> {
                        // no-op
                    }
                }
            }
            hasNameFilter = nameFilter.isNotBlank()
            this@DebtorsFragment.contacts = contacts
            showAddDebtDialog.get(this)?.let {
                if (dontShowAddDebtDialog.not()) {
                    dontShowAddDebtDialog = true
                    showAddDebtDialog()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_CONTACTS_FOR_ADD_DEBT_DIALOG_PERMISSION_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intentionSubject
                    .onNext(
                        DebtorsIntention.OpenAddDebtDialog(
                            Manifest.permission.READ_CONTACTS,
                            READ_CONTACTS_FOR_ADD_DEBT_DIALOG_PERMISSION_CODE
                        )
                    )
            } else {
                showAddDebtDialog()
            }
            READ_CONTACTS_SYNC_PERMISSION_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intentionSubject.onNext(DebtorsIntention.SyncWithContacts)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun intentions() =
        Observable.merge(
            listOf(
                Observable.fromCallable {
                    DebtorsIntention.Init(
                        Manifest.permission.READ_CONTACTS,
                        READ_CONTACTS_SYNC_PERMISSION_CODE
                    )
                },
                intentionSubject,
                (fabView?.clicks() ?: Observable.empty<DebtorsIntention>())
                    .doOnNext { dontShowAddDebtDialog = false }
                    .map {
                        DebtorsIntention.OpenAddDebtDialog(
                            Manifest.permission.READ_CONTACTS,
                            READ_CONTACTS_FOR_ADD_DEBT_DIALOG_PERMISSION_CODE
                        )
                    }
            )
        )

    private fun showAddDebtDialog() {
        if (context == null) return
        addDebtLayout = AddDebtLayout(
            context!!,
            contacts = contacts
        )
        context?.showAlert(
            customView = addDebtLayout,
            titleResId = R.string.home_debtors_dialog_add_debt,
            positiveButtonResId = R.string.home_debtors_dialog_confirm
        ) {
            addDebtLayout?.data?.let { data ->
                if (data.name.isNotBlank() && data.amount != 0.0) {
                    intentionSubject.onNext(
                        DebtorsIntention.AddDebt(
                            data.contactId,
                            data.name,
                            data.amount,
                            data.comment
                        )
                    )
                } else if (fabView != null) {
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
}
