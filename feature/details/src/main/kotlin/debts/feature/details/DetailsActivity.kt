package debts.feature.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import debts.core.common.android.BaseActivity
import debts.core.common.android.buildconfig.BuildConfigData
import debts.core.common.android.navigation.ActivityScreenContext
import debts.core.common.exeptions.NotExistsException
import debts.core.resource.theme.AppTheme
import debts.feature.adddebt.AddOrEditDebtDialogHolder
import debts.feature.adddebt.DebtLayoutData
import kotlinx.coroutines.launch
import net.thebix.debts.feature.details.R
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import net.thebix.debts.core.resource.R as ResourceR

class DetailsActivity : BaseActivity() {

    companion object {

        private const val KEY_DEBTOR_ID = "KEY_DEBTOR_ID"

        @JvmStatic
        fun createIntent(context: Context, debtorId: Long) = Intent(context, DetailsActivity::class.java)
            .apply {
                putExtra(KEY_DEBTOR_ID, debtorId)
            }
    }

    private val viewModel: DetailsViewModel by viewModel {
        parametersOf(intent?.extras?.getLong(KEY_DEBTOR_ID) ?: throw NotExistsException)
    }
    private val buildConfigData: BuildConfigData by inject()
    private val snackbarHostState = SnackbarHostState()
    private val screenContext by lazy {
        ActivityScreenContext(activity = this, applicationId = buildConfigData.getApplicationId())
    }

    private var addOrEditDebtDialogHolder: AddOrEditDebtDialogHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addOrEditDebtDialogHolder = AddOrEditDebtDialogHolder(
            this,
            object : AddOrEditDebtDialogHolder.AddOrEditDebtDialogHolderCallback {
                override fun onConfirm(data: DebtLayoutData) {
                    viewModel.onDebtDialogConfirmed(data)
                }
            }
        )
        val actions = DetailsActions(
            onBackClicked = { onBackPressedDispatcher.onBackPressed() },
            onShareClicked = {
                viewModel.onShareClicked(
                    getString(ResourceR.string.details_share_message_borrowed),
                    getString(ResourceR.string.details_share_message_lent),
                )
            },
            onRemoveDebtorConfirmed = viewModel::onRemoveDebtorConfirmed,
            onChangeClicked = viewModel::onChangeClicked,
            onClearHistoryConfirmed = viewModel::onClearHistoryConfirmed,
            onDebtClicked = viewModel::onDebtClicked,
            onRemoveDebtConfirmed = viewModel::onRemoveDebtConfirmed,
        )
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            AppTheme {
                DetailsScreen(uiState = uiState, snackbarHostState = snackbarHostState, actions = actions)
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

    override fun finish() {
        super.finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, ResourceR.anim.fade_out_activity)
    }

    private fun handleEvent(event: DetailsEvent) {
        when (event) {
            is DetailsEvent.ShowAddDebtDialog -> addOrEditDebtDialogHolder?.showAddDebt(
                name = event.name,
                avatarUrl = event.avatarUrl,
                contacts = emptyList(),
                canChangeDebtor = false,
            )

            is DetailsEvent.ShowEditDebtDialog -> addOrEditDebtDialogHolder?.showEditDebt(
                name = event.name,
                avatarUrl = event.avatarUrl,
                comment = event.comment,
                amount = event.amount,
                date = event.date,
                existingDebtId = event.debtId,
            )

            DetailsEvent.DebtAdded -> showToast(ResourceR.string.home_debtors_toast_debt_added)
            DetailsEvent.DebtChanged -> showToast(ResourceR.string.home_debtors_toast_debt_changed)
            DetailsEvent.EmptyDebtAmount -> lifecycleScope.launch {
                snackbarHostState.showSnackbar(getString(R.string.details_empty_debt_fields))
            }

            is DetailsEvent.ShareDebtor -> screenContext.sendExplicit(
                getString(ResourceR.string.details_share_title),
                event.message,
            )

            DetailsEvent.Close -> finish()
        }
    }

    private fun showToast(textId: Int) {
        Toast.makeText(this, textId, Toast.LENGTH_SHORT).show()
    }
}
