package debts.feature.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import debts.core.common.android.extensions.toFormattedCurrency
import debts.core.common.android.extensions.toSimpleDateTimeString
import debts.core.resource.component.NavigationBarBackground
import debts.core.usecase.data.DebtItemModel
import net.thebix.debts.feature.details.R
import java.util.Date
import kotlin.math.absoluteValue
import net.thebix.debts.core.common.R as CommonR
import net.thebix.debts.core.resource.R as ResourceR

/**
 * @param initialCollapseFraction how far the header starts collapsed, from 0 (expanded) to 1 (collapsed);
 * screenshot tests use it to capture both ends without a scroll gesture.
 */
@Composable
fun DetailsScreen(
    uiState: DetailsUiState,
    snackbarHostState: SnackbarHostState,
    actions: DetailsActions,
    initialCollapseFraction: Float = 0f,
) {
    val collapseState = rememberSaveable(saver = DetailsCollapseState.Saver) {
        DetailsCollapseState(initialCollapseFraction)
    }
    var debtIdToRemove by rememberSaveable { mutableStateOf<Long?>(null) }
    var isClearConfirmationShown by rememberSaveable { mutableStateOf(false) }
    var isRemoveDebtorConfirmationShown by rememberSaveable { mutableStateOf(false) }
    val headerActions = DetailsHeaderActions(
        onBackClicked = actions.onBackClicked,
        onShareClicked = actions.onShareClicked,
        onRemoveDebtorClicked = { isRemoveDebtorConfirmationShown = true },
        onChangeClicked = actions.onChangeClicked,
        onClearClicked = { isClearConfirmationShown = true },
    )

    Scaffold(
        modifier = Modifier.nestedScroll(collapseState.nestedScrollConnection),
        topBar = {
            DetailsHeader(
                uiState = uiState,
                fraction = collapseState.fraction,
                actions = headerActions,
                onCollapsibleHeightChanged = { collapseState.collapsibleHeightPx = it },
            )
        },
        bottomBar = { NavigationBarBackground() },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        DebtsList(
            debts = uiState.debts,
            onDebtClicked = actions.onDebtClicked,
            onRemoveDebtClicked = { debtIdToRemove = it },
            modifier = Modifier.padding(paddingValues),
        )
    }
    debtIdToRemove?.let { debtId ->
        ConfirmationDialog(
            messageRes = R.string.details_remove_debt_message,
            onConfirm = { actions.onRemoveDebtConfirmed(debtId) },
            onDismiss = { debtIdToRemove = null },
        )
    }
    if (isClearConfirmationShown) {
        ConfirmationDialog(
            messageRes = R.string.details_clear_all_dialog_confirmation_message,
            onConfirm = actions.onClearHistoryConfirmed,
            onDismiss = { isClearConfirmationShown = false },
        )
    }
    if (isRemoveDebtorConfirmationShown) {
        ConfirmationDialog(
            messageRes = ResourceR.string.details_dialog_delete_message,
            onConfirm = actions.onRemoveDebtorConfirmed,
            onDismiss = { isRemoveDebtorConfirmationShown = false },
        )
    }
}

@Composable
private fun DebtsList(
    debts: List<DebtItemModel>,
    onDebtClicked: (Long) -> Unit,
    onRemoveDebtClicked: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize()) {
        items(debts, key = { it.id }) { debt ->
            DebtRow(
                debt = debt,
                onClick = { onDebtClicked(debt.id) },
                onRemoveClicked = { onRemoveDebtClicked(debt.id) },
            )
            HorizontalDivider(Modifier.padding(start = CONTENT_START))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DebtRow(debt: DebtItemModel, onClick: () -> Unit, onRemoveClicked: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        Column(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true })
                .padding(start = CONTENT_START, top = SCREEN_PADDING, end = SCREEN_PADDING, bottom = SCREEN_PADDING)
        ) {
            Row {
                Text(
                    text = stringResource(
                        if (debt.amount < 0) {
                            R.string.details_debts_item_sign_borrowed
                        } else {
                            R.string.details_debts_item_sign_lent
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.alignByBaseline(),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(
                        R.string.details_debt_amount,
                        debt.currency,
                        debt.amount.absoluteValue.toFormattedCurrency(),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            Text(
                text = Date(debt.date).toSimpleDateTimeString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (debt.comment.isNotBlank()) {
                Text(text = debt.comment, style = MaterialTheme.typography.bodyMedium)
            }
        }
        DebtMenu(
            expanded = menuExpanded,
            onDismiss = { menuExpanded = false },
            onRemoveClicked = onRemoveClicked,
            onChangeClicked = onClick,
        )
    }
}

@Composable
private fun DebtMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRemoveClicked: () -> Unit,
    onChangeClicked: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.details_debts_item_menu_remove)) },
            onClick = {
                onDismiss()
                onRemoveClicked()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.details_debts_item_menu_change)) },
            onClick = {
                onDismiss()
                onChangeClicked()
            },
        )
    }
}

@Composable
private fun ConfirmationDialog(messageRes: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(CommonR.string.default_dialog_title)) },
        text = { Text(stringResource(messageRes)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onConfirm()
                }
            ) { Text(stringResource(CommonR.string.default_positive_button)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(CommonR.string.default_negative_button)) }
        },
    )
}
