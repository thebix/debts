package debts.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import debts.core.repository.SortType
import net.thebix.debts.feature.home.R
import net.thebix.debts.core.resource.R as ResourceR

private const val PLACEHOLDER_ALPHA = 0.7f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTopBar(uiState: HomeUiState, actions: HomeActions) {
    if (uiState.isSearchActive) {
        TopAppBar(
            title = { SearchField(uiState.searchQuery, actions.onSearchQueryChanged) },
            navigationIcon = {
                IconButton(onClick = actions.onSearchClosed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { actions.onSearchQueryChanged("") }) {
                        Icon(painterResource(ResourceR.drawable.ic_clear), contentDescription = null)
                    }
                }
            },
            colors = homeTopBarColors(),
        )
    } else {
        TopAppBar(
            title = { Text(stringResource(ResourceR.string.app_name)) },
            actions = {
                IconButton(onClick = actions.onSearchOpened) {
                    Icon(
                        painterResource(ResourceR.drawable.ic_search),
                        contentDescription = stringResource(R.string.home_debtors_menu_search),
                    )
                }
                SortMenu(uiState.sortType, actions)
                OverflowMenu(actions)
            },
            colors = homeTopBarColors(),
        )
    }
}

// Same colours as the Preferences app bar; the status bar icons are light, so the bar must stay dark.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun homeTopBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
)

@Composable
private fun SearchField(query: String, onQueryChanged: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val placeholderColor = contentColor.copy(alpha = PLACEHOLDER_ALPHA)
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        placeholder = { Text(stringResource(R.string.home_debtors_search_hint)) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = contentColor,
            unfocusedTextColor = contentColor,
            cursorColor = contentColor,
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
        ),
    )
}

@Composable
private fun SortMenu(sortType: SortType, actions: HomeActions) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painterResource(ResourceR.drawable.ic_sort),
                contentDescription = stringResource(R.string.home_debtors_menu_sort),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.home_debtors_menu_sort_name)) },
                onClick = {
                    expanded = false
                    actions.onSortByNameClicked()
                },
                trailingIcon = { SortStateIcon(sortType, asc = SortType.NAME_ASC, desc = SortType.NAME_DESC) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.home_debtors_menu_sort_amount)) },
                onClick = {
                    expanded = false
                    actions.onSortByAmountClicked()
                },
                trailingIcon = { SortStateIcon(sortType, asc = SortType.AMOUNT_ASC, desc = SortType.AMOUNT_DESC) },
            )
        }
    }
}

/**
 * Same icons as the old menu: an up arrow while ascending, a cross while descending
 * (the next tap turns the sorting off) and a down arrow when this sorting is off.
 */
@Composable
private fun SortStateIcon(sortType: SortType, asc: SortType, desc: SortType) {
    Icon(
        painterResource(
            when (sortType) {
                asc -> ResourceR.drawable.ic_arrow_drop_up
                desc -> ResourceR.drawable.ic_clear
                else -> ResourceR.drawable.ic_arrow_drop_down
            }
        ),
        contentDescription = null,
    )
}

@Composable
private fun OverflowMenu(actions: HomeActions) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(ResourceR.string.details_menu_share)) },
                onClick = {
                    expanded = false
                    actions.onShareAllClicked()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.home_debtors_menu_settings)) },
                onClick = {
                    expanded = false
                    actions.onSettingsClicked()
                },
            )
        }
    }
}
