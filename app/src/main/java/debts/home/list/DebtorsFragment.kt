package debts.home.list

import android.os.Bundle
import android.support.annotation.UiThread
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import debts.common.android.BaseFragment
import debts.common.android.bindView
import debts.home.list.adapter.DebtorsAdapter
import debts.home.list.mvi.DebtorsIntention
import debts.home.list.mvi.DebtorsState
import debts.home.list.mvi.DebtorsViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import okb.common.android.extension.getDrawableCompat
import okb.common.android.extension.showAlert
import org.koin.android.viewmodel.ext.viewModel
import timber.log.Timber
import net.thebix.debts.R

class DebtorsFragment : BaseFragment() {

    private val recyclerView by bindView<RecyclerView>(R.id.home_debtors_recycler)
    private val fabView by bindView<View>(R.id.home_debtors_fab)

    private val viewModel: DebtorsViewModel by viewModel()
    private val adapter = DebtorsAdapter()
    private val intentionSubject = PublishSubject.create<DebtorsIntention>()

    private lateinit var disposables: CompositeDisposable
    private var adapterDisposable: Disposable? = null
    private var isConfigurationChange: Boolean = false
    private lateinit var addDebtLayout: AddDebtLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isConfigurationChange = savedInstanceState != null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.home_debtors_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.apply {
            adapter = this@DebtorsFragment.adapter
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
            fabView.clicks()
                .subscribe {
                    intentionSubject.onNext(DebtorsIntention.GetContacts)
                }
        )
    }

    override fun onStop() {
        disposables.dispose()
        adapterDisposable?.dispose()
        super.onStop()
    }

    @UiThread
    private fun render(state: DebtorsState) {
        Timber.d("State is: $state")
        with(state) {
            adapterDisposable?.dispose()
            if (isConfigurationChange) {
                adapter.replaceAllItems(items)
                isConfigurationChange = false
            } else {
                adapterDisposable = adapter.setItems(items)
                    .subscribe()
            }
            contacts.get(this)?.let { contacts ->
                if (context == null) return@let
                addDebtLayout = AddDebtLayout(
                    context!!,
                    contacts = contacts
                )
                context?.showAlert(
                    addDebtLayout,
                    titleResId = R.string.home_debtors_dialog_add_debt,
                    positiveButtonResId = R.string.home_debtors_dialog_confirm,
                    negativeButtonResId = R.string.home_debtors_dialog_cancel,
                    actionPositive = {
                        with(addDebtLayout.data) {
                            intentionSubject.onNext(
                                DebtorsIntention.AddDebt(
                                    contactId,
                                    name,
                                    amount,
                                    currency,
                                    comment
                                )
                            )
                        }
                    }
                )
            }
        }
    }

    private fun intentions() =
        Observable.merge(
            listOf(
                Observable.fromCallable { DebtorsIntention.Init },
                intentionSubject
            )
        )
}
