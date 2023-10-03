package debts.preferences.main

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.widget.Toolbar
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import debts.common.android.FragmentScreenContext
import debts.core.common.android.BaseActivity
import debts.core.common.android.buildconfig.BuildConfigData
import debts.core.common.android.extensions.findViewById
import debts.core.common.android.navigation.ScreenContextHolder
import debts.preferences.main.mvi.MainSettingsIntention
import debts.preferences.main.mvi.MainSettingsState
import debts.preferences.main.mvi.MainSettingsViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import net.thebix.debts.R
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainSettingsFragment : PreferenceFragmentCompat() {

    private companion object {

        const val READ_CONTACTS_SYNC_PERMISSION_CODE = 3
    }

    private val screenContextHolder: ScreenContextHolder by inject()
    private val buildConfigData: BuildConfigData by inject()
    private val viewModel: MainSettingsViewModel by viewModel()
    private val intentionSubject = PublishSubject.create<MainSettingsIntention>()

    private var toolbarView: Toolbar? = null

    private var currencyListPref: ListPreference? = null
    private var currencyCustomPref: EditTextPreference? = null
    private var syncContactsPref: Preference? = null

    private lateinit var disposables: CompositeDisposable

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_main_settings, rootKey)
    }

    private fun updateCurrencyCustomSummary(text: String) {
        currencyCustomPref?.summary = context?.getString(
            R.string.preference_main_settings_currency_summary,
            text
        )
    }

    private fun updateCurrencySummary(text: String) {
        currencyListPref?.summary = context?.getString(
            R.string.preference_main_settings_currency_summary,
            text
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbarView = findViewById(R.id.settings_toolbar)

        (activity as BaseActivity).setSupportActionBar(toolbarView)
        (activity as BaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        currencyListPref = preferenceScreen.findPreference("preference_main_settings_currency")
        currencyCustomPref =
            preferenceScreen.findPreference("preference_main_settings_currency_custom")
        syncContactsPref = preferenceScreen.findPreference("preference_main_settings_sync_contacts")

        preferenceScreen.findPreference<Preference>("preference_main_settings_version")?.summary =
            buildConfigData.getVersionName()

        currencyCustomPref?.isVisible = currencyListPref?.value == "Custom"

        // region Tech debt

        /**
         * Tech debt:
         *      - selected currency should be returned from repository/interactor
         *      - updateCurrencyCustomSummary should be called from render
         *      - pref clicks should be wrapped with RxBind if it's possible
         *      - use constants
         */
        val preferences = PreferenceManager.getDefaultSharedPreferences(view.context.applicationContext)
        updateCurrencyCustomSummary(
            preferences.getString(
                "preference_main_settings_currency_custom",
                ""
            ) ?: ""
        )

        currencyListPref?.setOnPreferenceChangeListener { _, newValue ->
            currencyCustomPref?.isVisible = (newValue == "Custom")
            if (newValue != "Custom") {
                val value = newValue as? String ?: ""
                currencyCustomPref?.text = value
                updateCurrencyCustomSummary(value)
                intentionSubject.onNext(MainSettingsIntention.UpdateCurrency(value))
            }
            updateCurrencySummary(newValue as? String ?: "")
            true
        }

        currencyCustomPref?.setOnPreferenceChangeListener { _, newValue ->
            val value = newValue as? String ?: ""
            updateCurrencyCustomSummary(value)
            intentionSubject.onNext(MainSettingsIntention.UpdateCurrency(value))
            true
        }

        syncContactsPref?.setOnPreferenceClickListener {
            intentionSubject.onNext(
                MainSettingsIntention.SyncWithContacts(
                    android.Manifest.permission.READ_CONTACTS,
                    READ_CONTACTS_SYNC_PERMISSION_CODE
                )
            )
            true
        }

        // endregion
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
        toolbarView = null
        currencyListPref = null
        currencyCustomPref = null
        syncContactsPref = null
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
        with(state) {
            when (updateCurrencyState.get(this)) {
                MainSettingsState.UpdateState.START -> {
                }

                MainSettingsState.UpdateState.END -> {
                }

                MainSettingsState.UpdateState.ERROR -> {
                    if (toolbarView != null) {
                        Snackbar.make(
                            toolbarView!!,
                            R.string.preference_main_settings_state_error,
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
                }

                null -> {}
            }
            when (syncWithContactsState.get(this)) {
                MainSettingsState.UpdateState.START -> {
                    syncContactsPref?.summary =
                        context?.getString(R.string.preference_main_settings_state_updating)
                }

                MainSettingsState.UpdateState.END -> {
                    syncContactsPref?.summary =
                        context?.getString(R.string.preference_main_settings_state_updated)
                }

                MainSettingsState.UpdateState.ERROR -> {
                    syncContactsPref?.summary =
                        context?.getString(R.string.preference_main_settings_state_error)
                }

                null -> {}
            }
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
