package debts.feature.home

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import debts.core.common.android.BaseActivity
import debts.core.common.android.buildconfig.BuildConfigData
import debts.core.common.android.extensions.isPermissionGranted
import debts.core.common.android.navigation.ActivityScreenContext
import debts.core.resource.theme.AppTheme
import debts.feature.adddebt.AddOrEditDebtDialogHolder
import debts.feature.adddebt.DebtLayoutData
import kotlinx.coroutines.launch
import net.thebix.debts.feature.home.R
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import net.thebix.debts.core.resource.R as ResourceR

class HomeActivity : BaseActivity() {

    private companion object {

        const val CSV_FILE_NAME = "debts.csv"
        const val CSV_MIME_TYPE = "text/csv"
    }

    private val viewModel: HomeViewModel by viewModel()
    private val homeNavigator: HomeNavigator by inject()
    private val buildConfigData: BuildConfigData by inject()
    private val snackbarHostState = SnackbarHostState()
    private val screenContext by lazy {
        ActivityScreenContext(activity = this, applicationId = buildConfigData.getApplicationId())
    }

    private val addDebtPermissionLauncher = contactsPermissionLauncher(ContactsPermissionPurpose.AddDebt)
    private val syncPermissionLauncher = contactsPermissionLauncher(ContactsPermissionPurpose.Sync)

    private var addOrEditDebtDialogHolder: AddOrEditDebtDialogHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addOrEditDebtDialogHolder = AddOrEditDebtDialogHolder(
            this,
            object : AddOrEditDebtDialogHolder.AddOrEditDebtDialogHolderCallback {
                override fun onConfirm(data: DebtLayoutData) {
                    viewModel.onAddDebtConfirmed(data)
                }
            }
        )
        val actions = HomeActions(
            onSearchOpened = viewModel::onSearchOpened,
            onSearchClosed = viewModel::onSearchClosed,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onSortByNameClicked = viewModel::onSortByNameClicked,
            onSortByAmountClicked = viewModel::onSortByAmountClicked,
            onShareAllClicked = viewModel::onShareAllClicked,
            onSettingsClicked = viewModel::onSettingsClicked,
            onAddDebtClicked = viewModel::onAddDebtClicked,
            onDebtorClicked = viewModel::onDebtorClicked,
            onShareDebtorClicked = { debtorId ->
                viewModel.onShareDebtorClicked(
                    debtorId,
                    getString(ResourceR.string.details_share_message_borrowed),
                    getString(ResourceR.string.details_share_message_lent),
                )
            },
            onRemoveDebtorConfirmed = viewModel::onRemoveDebtorConfirmed,
        )
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AppTheme {
                HomeScreen(uiState = uiState, snackbarHostState = snackbarHostState, actions = actions)
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect(::handleEvent)
            }
        }
    }

    override fun onDestroy() {
        addOrEditDebtDialogHolder = null
        super.onDestroy()
    }

    private fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OpenDetails -> startActivity(homeNavigator.detailsIntent(this, event.debtorId))
            HomeEvent.OpenSettings -> startActivity(homeNavigator.settingsIntent(this))
            is HomeEvent.ShareDebtor -> screenContext.sendExplicit(
                getString(ResourceR.string.details_share_title),
                event.message,
            )

            is HomeEvent.ShareAllDebts -> screenContext.sendExplicitFile(
                getString(R.string.home_debtors_share_title),
                CSV_FILE_NAME,
                event.csvContent,
                CSV_MIME_TYPE,
            )

            is HomeEvent.RequestContactsPermission -> requestContactsPermission(event.purpose)
            is HomeEvent.ShowAddDebtDialog -> addOrEditDebtDialogHolder?.showAddDebt(contacts = event.contacts)
            HomeEvent.DebtAdded ->
                Toast.makeText(this, ResourceR.string.home_debtors_toast_debt_added, Toast.LENGTH_SHORT).show()

            HomeEvent.EmptyDebtFields -> lifecycleScope.launch {
                snackbarHostState.showSnackbar(getString(R.string.home_debtors_empty_debt_fields))
            }
        }
    }

    private fun requestContactsPermission(purpose: ContactsPermissionPurpose) {
        if (isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
            viewModel.onContactsPermissionResult(purpose, isGranted = true)
        } else {
            when (purpose) {
                ContactsPermissionPurpose.AddDebt -> addDebtPermissionLauncher
                ContactsPermissionPurpose.Sync -> syncPermissionLauncher
            }.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun contactsPermissionLauncher(purpose: ContactsPermissionPurpose) =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.onContactsPermissionResult(purpose, isGranted)
        }
}
