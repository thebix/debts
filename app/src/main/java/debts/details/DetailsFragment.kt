package debts.details

import android.os.Bundle
import android.view.*
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
import debts.common.android.BaseFragment
import debts.common.android.FragmentArgumentDelegate
import debts.common.android.FragmentScreenContext
import debts.common.android.ScreenContextHolder
import debts.common.android.extensions.getDrawableCompat
import debts.common.android.extensions.showAlert
import debts.common.android.extensions.toFormattedCurrency
import debts.common.android.extensions.tryToGoBack
import debts.details.adapter.DebtItemLayout
import debts.details.adapter.DebtsAdapter
import debts.details.mvi.DetailsIntention
import debts.details.mvi.DetailsState
import debts.details.mvi.DetailsViewModel
import debts.home.AddDebtLayout
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import net.thebix.debts.R
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

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
    }

    private val screenContextHolder: ScreenContextHolder by inject()
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

    private lateinit var addDebtLayout: AddDebtLayout
    private var debtorId: Long  by FragmentArgumentDelegate()
    private lateinit var disposables: CompositeDisposable
    private var adapterDisposable: Disposable? = null
    private var avatarUrl: String = ""
    private var isBorrowed: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
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

        recyclerView?.apply {
            adapter = this@DetailsFragment.adapter
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(context.applicationContext, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(context.applicationContext.getDrawableCompat(R.drawable.list_divider_start_66dp))
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
                context?.showAlert(messageId = R.string.details_dialog_delete_message) {
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
                        resources.getString(R.string.details_share_title),
                        resources.getString(R.string.details_share_message_borrowed),
                        resources.getString(R.string.details_share_message_lent)
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
            FragmentScreenContext(this)
        )
        disposables = CompositeDisposable(
            viewModel.states()
                .subscribe(::render),
            viewModel.processIntentions(intentions()),
            changeView!!.clicks()
                .subscribe {
                    if (context == null) {
                        return@subscribe
                    }
                    addDebtLayout = AddDebtLayout(
                        context!!,
                        name = nameView?.text.toString(),
                        avatarUrl = avatarUrl
                    )
                    context?.showAlert(
                        customView = addDebtLayout,
                        titleResId = R.string.home_add_debt_title,
                        positiveButtonResId = R.string.home_add_debt_confirm
                    ) {
                        with(addDebtLayout.data) {
                            if (amount != 0.0) {
                                intentionSubject.onNext(
                                    DetailsIntention.AddDebt(
                                        debtorId,
                                        amount,
                                        comment
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
                },
            clearView!!.clicks()
                .subscribe {
                    context?.showAlert(
                        messageId = R.string.details_clear_all_dialog_confirmation_message,
                        actionPositive = {
                            intentionSubject.onNext(DetailsIntention.ClearHistory(debtorId))
                        }
                    )
                })
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
        super.onDestroyView()
    }

    @UiThread
    private fun render(state: DetailsState) {
        Timber.d("State is: $state")
        // TODO: make loader on add debt / load screen
        with(state) {
            adapter.replaceAllItems(items)
            nameView?.text = name
            amountView?.text = context?.getString(R.string.details_debt_amount, currency, amount.toFormattedCurrency())
            isBorrowed = amount < 0
            this@DetailsFragment.avatarUrl = avatarUrl
            if (avatarView != null) {
                Glide.with(avatarView!!)
                    .load(if (avatarUrl.isNotBlank()) avatarUrl else R.drawable.ic_launcher)
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_launcher)
                    .fallback(R.drawable.ic_launcher)
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
}
