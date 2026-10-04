package debts.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import debts.core.common.android.extensions.toFormattedCurrency
import debts.core.common.android.extensions.toSimpleDateString
import debts.core.resource.component.NavigationBarBackground
import debts.core.usecase.data.TabTypes
import kotlinx.coroutines.launch
import net.thebix.debts.feature.home.R
import java.util.Date
import net.thebix.debts.core.common.R as CommonR
import net.thebix.debts.core.resource.R as ResourceR

private val AVATAR_SIZE: Dp = 50.dp
private val SCREEN_PADDING: Dp = 16.dp
private val TOTAL_ROW_HEIGHT: Dp = 48.dp

/**
 * Callbacks of the Home screen. The defaults let previews and screenshot tests set only what they use.
 */
data class HomeActions(
    val onSearchOpened: () -> Unit = {},
    val onSearchClosed: () -> Unit = {},
    val onSearchQueryChanged: (String) -> Unit = {},
    val onSortByNameClicked: () -> Unit = {},
    val onSortByAmountClicked: () -> Unit = {},
    val onShareAllClicked: () -> Unit = {},
    val onSettingsClicked: () -> Unit = {},
    val onAddDebtClicked: () -> Unit = {},
    val onDebtorClicked: (Long) -> Unit = {},
    val onShareDebtorClicked: (Long) -> Unit = {},
    val onRemoveDebtorConfirmed: (Long) -> Unit = {},
)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    actions: HomeActions,
    initialTab: TabTypes = TabTypes.All,
) {
    val tabs = TabTypes.entries
    val pagerState = rememberPagerState(initialPage = tabs.indexOf(initialTab)) { tabs.size }
    val scope = rememberCoroutineScope()
    var debtorIdToRemove by rememberSaveable { mutableStateOf<Long?>(null) }

    BackHandler(enabled = uiState.isSearchActive, onBack = actions.onSearchClosed)
    Scaffold(
        topBar = { HomeTopBar(uiState, actions) },
        bottomBar = { NavigationBarBackground() },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = actions.onAddDebtClicked,
                modifier = Modifier.padding(bottom = TOTAL_ROW_HEIGHT),
            ) {
                Icon(painterResource(ResourceR.drawable.ic_add), contentDescription = null)
            }
        },
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(stringResource(tab.titleRes())) },
                    )
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                HomeTabPage(
                    tab = uiState.tabs[tabs[page]] ?: HomeTabUiState(),
                    currency = uiState.currency,
                    onDebtorClicked = actions.onDebtorClicked,
                    onShareDebtorClicked = actions.onShareDebtorClicked,
                    onRemoveDebtorClicked = { debtorIdToRemove = it },
                )
            }
        }
    }
    debtorIdToRemove?.let { debtorId ->
        RemoveDebtorDialog(
            onConfirm = {
                debtorIdToRemove = null
                actions.onRemoveDebtorConfirmed(debtorId)
            },
            onDismiss = { debtorIdToRemove = null },
        )
    }
}

private fun TabTypes.titleRes() = when (this) {
    TabTypes.All -> R.string.home_pager_tab_all
    TabTypes.Debtors -> R.string.home_pager_tab_debtors
    TabTypes.Creditors -> R.string.home_pager_tab_creditors
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeTabPage(
    tab: HomeTabUiState,
    currency: String,
    onDebtorClicked: (Long) -> Unit,
    onShareDebtorClicked: (Long) -> Unit,
    onRemoveDebtorClicked: (Long) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth()) {
            tab.items.forEach { item ->
                when (item) {
                    is HomeListItem.Header -> stickyHeader(key = item.section.name, contentType = "header") {
                        SectionHeader(item.section)
                    }

                    is HomeListItem.Debtor -> item(key = item.id, contentType = "debtor") {
                        DebtorRow(
                            debtor = item,
                            onClick = { onDebtorClicked(item.id) },
                            onShareClicked = { onShareDebtorClicked(item.id) },
                            onRemoveClicked = { onRemoveDebtorClicked(item.id) },
                        )
                        HorizontalDivider(Modifier.padding(start = SCREEN_PADDING + AVATAR_SIZE))
                    }
                }
            }
        }
        TotalRow(amount = tab.total, currency = currency)
    }
}

@Composable
private fun SectionHeader(section: HomeSection) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(
                when (section) {
                    HomeSection.Debtors -> R.string.home_pager_tab_debtors
                    HomeSection.Creditors -> R.string.home_pager_tab_creditors
                }
            ).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = SCREEN_PADDING, vertical = 8.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DebtorRow(
    debtor: HomeListItem.Debtor,
    onClick: () -> Unit,
    onShareClicked: () -> Unit,
    onRemoveClicked: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true })
                .padding(SCREEN_PADDING),
        ) {
            AsyncImage(
                model = debtor.avatarUrl.ifBlank { ResourceR.mipmap.ic_launcher },
                contentDescription = null,
                modifier = Modifier.size(AVATAR_SIZE).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(SCREEN_PADDING))
            DebtorTexts(debtor)
        }
        DebtorMenu(
            expanded = menuExpanded,
            onDismiss = { menuExpanded = false },
            onShareClicked = onShareClicked,
            onRemoveClicked = onRemoveClicked,
        )
    }
}

@Composable
private fun DebtorTexts(debtor: HomeListItem.Debtor) {
    Column {
        Text(
            text = debtor.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row {
            Text(
                text = stringResource(
                    R.string.home_debtors_item_amount,
                    debtor.currency,
                    debtor.amount.toFormattedCurrency(),
                ),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
            if (debtor.lastDate != Long.MIN_VALUE) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(
                        R.string.home_debtors_item_date,
                        Date(debtor.lastDate).toSimpleDateString(),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DebtorMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onShareClicked: () -> Unit,
    onRemoveClicked: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.home_debtors_item_menu_share)) },
            onClick = {
                onDismiss()
                onShareClicked()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.home_debtors_item_menu_remove)) },
            onClick = {
                onDismiss()
                onRemoveClicked()
            },
        )
    }
}

@Composable
private fun TotalRow(amount: Double, currency: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = SCREEN_PADDING, vertical = 12.dp),
        ) {
            Text(
                text = stringResource(ResourceR.string.details_debt_total).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.home_debtors_item_amount, currency, amount.toFormattedCurrency()),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RemoveDebtorDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(CommonR.string.default_dialog_title)) },
        text = { Text(stringResource(ResourceR.string.details_dialog_delete_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(CommonR.string.default_positive_button)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(CommonR.string.default_negative_button)) }
        },
    )
}
