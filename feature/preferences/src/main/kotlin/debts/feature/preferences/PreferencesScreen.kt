package debts.feature.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import debts.feature.preferences.PreferencesUiState.SyncStatus
import net.thebix.debts.feature.preferences.R

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    uiState: PreferencesUiState,
    snackbarHostState: SnackbarHostState,
    onCurrencySelected: (String) -> Unit,
    onCustomCurrencyChanged: (String) -> Unit,
    onCustomCurrencySubmitted: (String) -> Unit,
    onSyncWithContactsClicked: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                PreferenceSectionHeader(stringResource(R.string.preference_main_settings_general_title))
            }
            item {
                CurrencyPreferenceItem(
                    selectedCurrency = uiState.selectedCurrency,
                    onCurrencySelected = onCurrencySelected,
                )
            }
            if (uiState.isCustomCurrency) {
                item {
                    CustomCurrencyPreferenceItem(
                        text = uiState.customCurrencyText,
                        onTextChanged = onCustomCurrencyChanged,
                        onSubmitted = onCustomCurrencySubmitted,
                    )
                }
            }
            item {
                VersionPreferenceItem(versionName = uiState.versionName)
            }
            item {
                PreferenceSectionHeader(stringResource(R.string.preference_main_settings_sync_title))
            }
            item {
                SyncWithContactsPreferenceItem(
                    syncStatus = uiState.syncStatus,
                    onClick = onSyncWithContactsClicked,
                )
            }
        }
    }
}

@Composable
private fun PreferenceSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun CurrencyPreferenceItem(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val allOptions = remember { listOf(PreferencesUiState.CUSTOM_KEY) + PreferencesUiState.STANDARD_CURRENCIES }
    val customLabel = stringResource(R.string.preference_main_settings_currency_custom)
    val displayValue = when {
        selectedCurrency.isEmpty() -> ""
        selectedCurrency == PreferencesUiState.CUSTOM_KEY -> customLabel
        else -> selectedCurrency
    }

    ListItem(
        headlineContent = { Text(stringResource(R.string.preference_main_settings_currency)) },
        supportingContent = {
            if (displayValue.isNotEmpty()) {
                Text(stringResource(R.string.preference_main_settings_currency_summary, displayValue))
            }
        },
        trailingContent = {
            Box {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    allOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(if (option == PreferencesUiState.CUSTOM_KEY) customLabel else option) },
                            onClick = {
                                expanded = false
                                onCurrencySelected(option)
                            },
                        )
                    }
                }
            }
        },
        modifier = Modifier.clickable { expanded = true },
    )
}

@Composable
private fun CustomCurrencyPreferenceItem(
    text: String,
    onTextChanged: (String) -> Unit,
    onSubmitted: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    ListItem(
        headlineContent = {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                label = { Text(stringResource(R.string.preference_main_settings_currency_custom)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSubmitted(text)
                    }
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun VersionPreferenceItem(versionName: String) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.preference_main_settings_version)) },
        supportingContent = { Text(versionName) },
    )
}

@Composable
private fun SyncWithContactsPreferenceItem(
    syncStatus: SyncStatus,
    onClick: () -> Unit,
) {
    val subtitle = when (syncStatus) {
        SyncStatus.Idle -> stringResource(R.string.preference_main_settings_sync_contacts_summary)
        SyncStatus.Updating -> stringResource(R.string.preference_main_settings_state_updating)
        SyncStatus.Updated -> stringResource(R.string.preference_main_settings_state_updated)
        SyncStatus.Error -> stringResource(R.string.preference_main_settings_state_error)
    }
    ListItem(
        headlineContent = { Text(stringResource(R.string.preference_main_settings_sync_contacts)) },
        supportingContent = { Text(subtitle) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
