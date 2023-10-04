package debts.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import debts.core.common.android.BaseFragment
import debts.core.common.android.FragmentArgumentDelegate
import debts.core.common.android.buildconfig.BuildConfigData
import debts.core.common.android.extensions.getDrawableCompat
import debts.core.common.android.extensions.showAlert
import debts.core.common.android.extensions.toFormattedCurrency
import debts.core.common.android.extensions.tryToGoBack
import debts.core.common.android.navigation.FragmentScreenContext
import debts.core.common.android.navigation.ScreenContextHolder
import debts.details.adapter.DebtItemLayout
import debts.details.adapter.DebtsAdapter
import debts.details.mvi.DetailsIntention
import debts.details.mvi.DetailsState
import debts.details.mvi.DetailsViewModel
import debts.feature.adddebt.AddOrEditDebtDialogHolder
import debts.feature.adddebt.DebtLayoutData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import net.thebix.debts.R
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.Date

class DetailsFragment : BaseFragment() {

    companion object {

        @JvmStatic
        fun createInstance(debtorId: Long) = DetailsFragment()
            .apply {
                this.debtorId = debtorId
            }
    }

    private val menuItemClickListener = object : DebtItemLayout.HistoryItemCallback {
        override fun onDebtRemove(debtId: Long) {
            context?.showAlert(messageId = R.string.details_remove_debt_message) {
                intentionSubject.onNext(DetailsIntention.RemoveDebt(debtId))
            }
        }

        override fun onDebtEdit(debtId: Long) {
            intentionSubject.onNext(DetailsIntention.EditDebt(debtId))
        }
    }

    private val addOrEditDebtDialogHolderCallbacks = object : AddOrEditDebtDialogHolder.AddOrEditDebtDialogHolderCallback {

        override fun onConfirm(data: DebtLayoutData) {
            handleAddOrEditDialogConfirmation(data)
        }
    }

    private val screenContextHolder: ScreenContextHolder by inject()
    private val buildConfigData: BuildConfigData by inject()
    private val viewModel: DetailsViewModel by viewModel()
    private val adapter = DebtsAdapter(menuItemClickListener)
    private val intentionSubject = PublishSubject.create<DetailsIntention>()

    private var toolbarView: Toolbar? = null
    private var avatarView: ImageView? = null
    private var nameView: TextView? = null
    private var amountView: TextView? = null
    private var changeView: View? = null
    private var clearView: View? = null
    private var recyclerView: RecyclerView? = null

    private var addOrEditDebtDialogHolder: AddOrEditDebtDialogHolder? = null
    private var debtorId: Long by FragmentArgumentDelegate()
    private lateinit var disposables: CompositeDisposable
    private var adapterDisposable: Disposable? = null
    private var avatarUrl: String = ""
    private var isBorrowed: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) =
        inflater.inflate(R.layout.details_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarView = getView()?.findViewById(R.id.details_toolbar)
        avatarView = getView()?.findViewById(R.id.details_avatar)
        nameView = getView()?.findViewById(R.id.details_name)
        amountView = getView()?.findViewById(R.id.details_debt_amount)
        changeView = getView()?.findViewById(R.id.details_debt_change)
        clearView = getView()?.findViewById(R.id.details_debt_clear)
        recyclerView = getView()?.findViewById(R.id.details_history)

        toolbarView?.title = ""
        (activity as AppCompatActivity).setSupportActionBar(toolbarView)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        addOrEditDebtDialogHolder = AddOrEditDebtDialogHolder(activity as AppCompatActivity, addOrEditDebtDialogHolderCallbacks)

        recyclerView?.apply {
            adapter = this@DetailsFragment.adapter
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    context.applicationContext,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(context.applicationContext.getDrawableCompat(net.thebix.debts.core.resource.R.drawable.list_divider_start_66dp))
                }
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_details_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
            }

            R.id.home_details_menu_delete -> {
                context?.showAlert(messageId = net.thebix.debts.core.resource.R.string.details_dialog_delete_message) {
                    intentionSubject.onNext(
                        DetailsIntention.RemoveDebtor(debtorId)
                    )
                }
                return true
            }

            R.id.home_details_menu_share -> {

                intentionSubject.onNext(
                    DetailsIntention.ShareDebtor(
                        debtorId,
                        resources.getString(net.thebix.debts.core.resource.R.string.details_share_title),
                        resources.getString(net.thebix.debts.core.resource.R.string.details_share_message_borrowed),
                        resources.getString(net.thebix.debts.core.resource.R.string.details_share_message_lent)
                    )
                )

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()

        screenContextHolder.set(
            ScreenContextHolder.FRAGMENT_DETAILS,
            FragmentScreenContext(
                fragment = this,
                applicationId = buildConfigData.getApplicationId()
            )
        )
        disposables = CompositeDisposable(
            viewModel.states()
                .subscribe(::render),
            viewModel.processIntentions(intentions()),
            changeView!!.clicks()
                .subscribe {
                    showAddDebtLayout()
                },
            clearView!!.clicks()
                .subscribe {
                    context?.showAlert(
                        messageId = R.string.details_clear_all_dialog_confirmation_message,
                        actionPositive = {
                            intentionSubject.onNext(DetailsIntention.ClearHistory(debtorId))
                        }
                    )
                }
        )
    }

    override fun onStop() {
        screenContextHolder.remove(ScreenContextHolder.FRAGMENT_DETAILS)
        disposables.dispose()
        adapterDisposable?.dispose()
        super.onStop()
    }

    override fun onDestroyView() {
        toolbarView = null
        avatarView = null
        nameView = null
        amountView = null
        changeView = null
        clearView = null
        recyclerView = null
        addOrEditDebtDialogHolder = null
        super.onDestroyView()
    }

    @UiThread
    private fun render(state: DetailsState) {
        Timber.d("State is: $state")
        // TODO: make loader on add debt / load screen
        with(state) {
            adapter.replaceAllItems(items)
            nameView?.text = name
            amountView?.text = context?.getString(
                R.string.details_debt_amount,
                currency,
                amount.toFormattedCurrency()
            )
            isBorrowed = amount < 0
            this@DetailsFragment.avatarUrl = avatarUrl

            if (avatarView != null) {
                Glide.with(avatarView!!)
                    .load(if (avatarUrl.isNotBlank()) avatarUrl else net.thebix.debts.core.resource.R.mipmap.ic_launcher)
                    .placeholder(net.thebix.debts.core.resource.R.mipmap.ic_launcher)
                    .error(net.thebix.debts.core.resource.R.mipmap.ic_launcher)
                    .fallback(net.thebix.debts.core.resource.R.mipmap.ic_launcher)
                    .apply(RequestOptions.circleCropTransform())
                    .into(avatarView!!)
            }
            clearView?.isEnabled = items.isNotEmpty()
            // region INFO: this logic should be moved to navigation class called from interactor
            isDebtorRemoved.get(state)?.let { isRemoved ->
                if (isRemoved) {
                    context?.tryToGoBack()
                }
            }
            debtEdit.get(state)
                ?.let { editDebt ->
                    showAddDebtLayout(
                        comment = editDebt.comment,
                        amount = editDebt.amount,
                        existingDebtId = editDebt.debtId,
                        date = editDebt.date
                    )
                }
            // endregion
            // TODO: isError
        }
    }

    private fun intentions() =
        Observable.merge(
            listOf(
                Observable.fromCallable { DetailsIntention.Init(debtorId) },
                intentionSubject
            )
        )

    private fun showAddDebtLayout(
        comment: String = "",
        amount: Double = 0.0,
        date: Long = Date().time,
        existingDebtId: Long? = null,
    ) {
        if (existingDebtId == null) {
            addOrEditDebtDialogHolder?.showAddDebt(
                name = nameView?.text.toString(),
                avatarUrl = avatarUrl,
                contacts = emptyList(),
                canChangeDebtor = false
            )
        } else {
            addOrEditDebtDialogHolder?.showEditDebt(
                name = nameView?.text.toString(),
                avatarUrl = avatarUrl,
                amount = amount,
                comment = comment,
                existingDebtId = existingDebtId,
                date = date
            )
        }
    }

    private fun handleAddOrEditDialogConfirmation(data: DebtLayoutData) {
        with(data) {
            if (this.amount != 0.0) {
                intentionSubject.onNext(
                    this.existingDebtId?.let {
                        DetailsIntention.EditDebtSave(
                            it,
                            this.amount,
                            this.comment,
                            this.date
                        )
                    } ?: DetailsIntention.AddDebt(
                        debtorId,
                        this.amount,
                        this.comment,
                        this.date
                    )
                )
            } else {
                Snackbar
                    .make(
                        changeView!!,
                        R.string.details_empty_debt_fields,
                        Snackbar.LENGTH_SHORT
                    )
                    .show()
            }
        }
    }
}
