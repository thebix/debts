package debts.home.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import debts.common.android.BaseFragment
import debts.common.android.FragmentArgumentDelegate
import debts.common.android.extensions.findViewById
import debts.home.details.adapter.DebtsAdapter
import debts.home.details.mvi.DetailsIntention
import debts.home.details.mvi.DetailsState
import debts.home.details.mvi.DetailsViewModel
import debts.home.list.AddDebtLayout
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import okb.common.android.extension.getDrawableCompat
import okb.common.android.extension.showAlert
import org.koin.android.viewmodel.ext.viewModel
import timber.log.Timber
import net.thebix.debts.R

class DetailsFragment : BaseFragment() {

    companion object {

        @JvmStatic
        fun createInstance(debtorId: Long) = DetailsFragment()
            .apply {
                this.debtorId = debtorId
            }
    }

    private var avatarView: ImageView? = null
    private var nameView: TextView? = null
    private var amountView: TextView? = null
    private var changeView: View? = null
    private var clearView: View? = null
    private var recyclerView: RecyclerView? = null

    private val viewModel: DetailsViewModel by viewModel()
    private val adapter = DebtsAdapter()
    private val intentionSubject = PublishSubject.create<DetailsIntention>()

    private lateinit var addDebtLayout: AddDebtLayout
    private var debtorId: Long  by FragmentArgumentDelegate()
    private lateinit var disposables: CompositeDisposable
    private var adapterDisposable: Disposable? = null
    private var avatarUrl: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.home_details_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        avatarView = findViewById(R.id.home_details_avatar)
        nameView = findViewById(R.id.home_details_name)
        amountView = findViewById(R.id.home_details_debt_amount)
        changeView = findViewById(R.id.home_details_debt_change)
        clearView = findViewById(R.id.home_details_debt_clear)
        recyclerView = findViewById(R.id.home_details_history)

        recyclerView?.apply {
            adapter = this@DetailsFragment.adapter
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(context.applicationContext, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(context.applicationContext.getDrawableCompat(R.drawable.list_divider))
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()

        disposables = CompositeDisposable(
            viewModel.states()
                .subscribe(::render),
            viewModel.processIntentions(intentions()),
            changeView!!.clicks()
                .subscribe {
                    addDebtLayout = AddDebtLayout(
                        context!!,
                        name = nameView?.text.toString(),
                        avatarUrl = avatarUrl

                    )
                    context?.showAlert(
                        addDebtLayout,
                        titleResId = R.string.home_debtors_dialog_add_debt,
                        positiveButtonResId = R.string.home_debtors_dialog_confirm,
                        negativeButtonResId = R.string.home_debtors_dialog_cancel,
                        actionPositive = {
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
                    )
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
            amountView?.text = context?.getString(R.string.home_details_debt_amount, amount, currency)
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
