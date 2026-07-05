package debts.feature.preferences

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import debts.core.common.android.BaseActivity
import debts.core.resource.theme.AppTheme
import net.thebix.debts.feature.preferences.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class PreferencesActivity : BaseActivity() {

    companion object {
        @JvmStatic
        fun createIntent(context: Context) = Intent(context, PreferencesActivity::class.java)
    }

    private val preferencesViewModel: PreferencesViewModel by viewModel()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) preferencesViewModel.onPermissionGranted()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            preferencesViewModel.init()
        }
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val uiState by preferencesViewModel.uiState.collectAsState()
            LaunchedEffect(Unit) {
                preferencesViewModel.events.collect { event ->
                    when (event) {
                        is PreferencesEvent.RequestPermission ->
                            requestPermissionLauncher.launch(event.permission)
                        PreferencesEvent.ShowError ->
                            snackbarHostState.showSnackbar(
                                message = getString(R.string.preference_main_settings_state_error)
                            )
                    }
                }
            }
            AppTheme {
                PreferencesScreen(
                    uiState = uiState,
                    snackbarHostState = snackbarHostState,
                    onCurrencySelected = preferencesViewModel::onCurrencySelected,
                    onCustomCurrencyChanged = preferencesViewModel::onCustomCurrencyChanged,
                    onCustomCurrencySubmitted = preferencesViewModel::onCustomCurrencySubmitted,
                    onSyncWithContactsClicked = preferencesViewModel::onSyncWithContactsClicked,
                    onNavigateUp = ::onBackPressed,
                )
            }
        }
    }
}
