package debts.feature.preferences

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import debts.core.common.android.buildconfig.BuildConfigData
import debts.core.common.android.navigation.FragmentScreenContext
import debts.core.common.android.navigation.ScreenContextHolder
import debts.feature.preferences.mvi.MainSettingsIntention
import debts.feature.preferences.mvi.MainSettingsState
import debts.feature.preferences.mvi.MainSettingsViewModel
import debts.feature.preferences.ui.main.SettingsScreen
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import net.thebix.debts.feature.preferences.R
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainSettingsFragment : Fragment() {

    private companion object {

        const val READ_CONTACTS_SYNC_PERMISSION_CODE = 3
    }

    private val screenContextHolder: ScreenContextHolder by inject()
    private val buildConfigData: BuildConfigData by inject()
    private val viewModel: MainSettingsViewModel by viewModel()
    private val intentionSubject = PublishSubject.create<MainSettingsIntention>()

    private var composeContainer: ComposeView? = null

    private lateinit var disposables: CompositeDisposable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        inflater.inflate(R.layout.main_settings_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        composeContainer = getView()?.findViewById(R.id.main_settings_compose_container)
    }

    override fun onStart() {
        super.onStart()
        screenContextHolder.set(
            ScreenContextHolder.FRAGMENT_MAIN_PREFERENCES,
            FragmentScreenContext(
                fragment = this,
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
        disposables.dispose()
        screenContextHolder.remove(ScreenContextHolder.FRAGMENT_MAIN_PREFERENCES)
        super.onStop()
    }

    override fun onDestroyView() {
        composeContainer = null
        super.onDestroyView()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            READ_CONTACTS_SYNC_PERMISSION_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intentionSubject.onNext(
                    MainSettingsIntention.SyncWithContacts(
                        android.Manifest.permission.READ_CONTACTS,
                        READ_CONTACTS_SYNC_PERMISSION_CODE
                    )
                )
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @UiThread
    private fun render(state: MainSettingsState) {
        Timber.d("State is: $state")
        composeContainer?.setContent {
            SettingsScreen(
                state = state,
                onBackClick = {
                    activity?.onBackPressed()
                },
                onCurrencyClick = { intentionSubject.onNext(MainSettingsIntention.ShowCurrencyDialog) },
                onCustomCurrencyClick = { intentionSubject.onNext(MainSettingsIntention.ShowCustomCurrencyDialog) },
                onSyncClick = {
                    intentionSubject.onNext(
                        MainSettingsIntention.SyncWithContacts(
                            Manifest.permission.READ_CONTACTS,
                            READ_CONTACTS_SYNC_PERMISSION_CODE
                        )
                    )
                },
                onCurrencyDialogClick = { value ->
                    intentionSubject.onNext(MainSettingsIntention.UpdateCurrencyListSelection(value))
                    if (value != "Custom") {
                        intentionSubject.onNext(MainSettingsIntention.UpdateCurrency(value))
                    }
                },
                onCustomCurrencyDialogClick = { value ->
                    intentionSubject.onNext(MainSettingsIntention.UpdateCurrency(value))
                },
            )
        }
    }

    private fun intentions() =
        Observable.merge(
            listOf(
                Observable.fromCallable { MainSettingsIntention.Init },
                intentionSubject
            )
        )
}
