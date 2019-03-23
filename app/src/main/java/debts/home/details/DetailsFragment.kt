package debts.home.details

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import debts.common.android.BaseActivity
import debts.common.android.BaseFragment
import debts.common.android.FragmentArgumentDelegate
import debts.common.android.extensions.findViewById
import debts.home.details.adapter.DebtItemLayout
import debts.home.details.adapter.DebtsAdapter
import debts.home.details.mvi.DetailsIntention
import debts.home.details.mvi.DetailsState
import debts.home.details.mvi.DetailsViewModel
import debts.home.list.AddDebtLayout
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import debts.common.android.extensions.getDrawableCompat
import debts.common.android.extensions.showAlert
import org.koin.android.viewmodel.ext.viewModel
import timber.log.Timber
import net.thebix.debts.R
import debts.common.android.extensions.getColorCompat
import debts.common.android.extensions.tryToGoBack

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
            intentionSubject.onNext(DetailsIntention.RemoveDebt(debtId))
        }
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as BaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.home_details_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarView = findViewById(R.id.home_toolbar)
        avatarView = findViewById(R.id.home_details_avatar)
        nameView = findViewById(R.id.home_details_name)
        amountView = findViewById(R.id.home_details_debt_amount)
        changeView = findViewById(R.id.home_details_debt_change)
        clearView = findViewById(R.id.home_details_debt_clear)
        recyclerView = findViewById(R.id.home_details_history)

        setHasOptionsMenu(true)
        toolbarView?.title = ""
        toolbarView?.setBackgroundColor(context?.getColorCompat(R.color.debts_white) ?: 0)

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
        activity?.menuInflater?.inflate(R.menu.home_details_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_details_menu_delete -> {
                context?.showAlert { intentionSubject.onNext(DetailsIntention.RemoveDebtor(debtorId)) }
                return true
            }
            R.id.home_details_menu_share -> {
                // region INFO: this logic should be moved to navigation class called from interactor
                val message =
                    resources.getString(
                        if (isBorrowed)
                            R.string.home_details_share_message_borrowed else R.string.home_details_share_message_lent,
                        nameView?.text,
                        amountView?.text
                    )
                val sendIntent = Intent(Intent.ACTION_SEND)
                    .apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, message)
                        type = "text/plain"
                    }

                val title: String = resources.getString(R.string.home_details_share_title)
                val chooser: Intent = Intent.createChooser(sendIntent, title)
                if (activity != null && sendIntent.resolveActivity(activity!!.packageManager) != null) {
                    startActivity(chooser)
                }
                // endregion
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()

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
                        titleResId = R.string.home_debtors_dialog_add_debt,
                        positiveButtonResId = R.string.home_debtors_dialog_confirm
                    ) {
                        with(addDebtLayout.data) {
                            if (amount != 0.0) {
                                intentionSubject.onNext(
                                    DetailsIntention.AddDebt(
                                        debtorId,
                                        amount,
                                        currency,
                                        comment
                                    )
                                )
                            } else {
                                Snackbar
                                    .make(
                                        changeView!!,
                                        R.string.home_details_empty_debt_fields,
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
                        titleResId = R.string.home_details_clear_all_dialog_confirmation_title,
                        messageId = R.string.home_details_clear_all_dialog_confirmation_message,
                        actionPositive = {
                            intentionSubject.onNext(DetailsIntention.ClearHistory(debtorId))
                        }
                    )
                })
    }

    override fun onStop() {
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
            amountView?.text = context?.getString(R.string.home_details_debt_amount, currency, amount)
            isBorrowed = amount < 0
            this@DetailsFragment.avatarUrl = avatarUrl
            if (avatarUrl.isNotBlank() && avatarView != null) {
                Glide.with(avatarView!!)
                    .load(avatarUrl)
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
