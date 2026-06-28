package debts.feature.preferences.ui.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import debts.core.resource.theme.AppTheme
import debts.feature.preferences.mvi.MainSettingsState
import debts.feature.preferences.ui.dialogs.CurrencyDialog
import debts.feature.preferences.ui.dialogs.CustomCurrencyDialog

@Suppress("LongParameterList")
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    state: MainSettingsState,
    onBackClick: () -> Unit,
    onCurrencyClick: () -> Unit,
    onCustomCurrencyClick: () -> Unit,
    onSyncClick: () -> Unit,
    onCurrencyDialogClick: (value: String) -> Unit,
    onCustomCurrencyDialogClick: (value: String) -> Unit,
) {
    AppTheme {
        Surface(
            modifier = modifier,
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
            ) {
                DebtsTopAppBar(
                    onBackClick = onBackClick,
                )
                SettingsGeneralSection(
                    state = state,
                    onCurrencyClick = onCurrencyClick,
                    onCustomCurrencyClick = onCustomCurrencyClick,
                )
                SettingsSyncronizationSection(
                    state = state,
                    onSyncClick = onSyncClick,
                )
            }
        }
        if (state.showCurrencyDialog) {
            CurrencyDialog(
                selectedCurrencyValue = state.currencyListSelection,
                onClick = onCurrencyDialogClick
            )
        }
        if (state.showCustomCurrencyDialog) {
            CustomCurrencyDialog(
                selectedCurrencyValue = state.currency,
                onClick = onCustomCurrencyDialogClick
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        state = MainSettingsState(
            currency = "€",
            version = "2.6.1",
        ),
        onBackClick = {},
        onCurrencyClick = {},
        onCustomCurrencyClick = {},
        onSyncClick = {},
        onCurrencyDialogClick = {},
        onCustomCurrencyDialogClick = {},
    )
}

@Suppress("UnusedPrivateMember")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SettingsScreenPreviewDark() {
    SettingsScreen(
        state = MainSettingsState(
            currency = "Custom",
            version = "2.6.1"
        ),
        onBackClick = {},
        onCurrencyClick = {},
        onCustomCurrencyClick = {},
        onSyncClick = {},
        onCurrencyDialogClick = {},
        onCustomCurrencyDialogClick = {},
    )
}
