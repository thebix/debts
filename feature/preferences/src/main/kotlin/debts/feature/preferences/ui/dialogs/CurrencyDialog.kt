@file: Suppress("FunctionNaming")

package debts.feature.preferences.ui.dialogs

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import debts.core.resource.theme.AppTheme
import debts.feature.preferences.mvi.MainSettingsState
import net.thebix.debts.feature.preferences.R
import timber.log.Timber

@Suppress("LongMethod")
@Composable
fun CurrencyDialog(
    modifier: Modifier = Modifier,
    selectedCurrencyValue: String,
    onClick: (value: String) -> Unit,
) {
    val currencyTitles = stringArrayResource(id = R.array.preference_main_settings_entries)
    val currencyEntries = stringArrayResource(id = R.array.preference_main_settings_values)
    if (currencyTitles.size != currencyEntries.size) {
        Timber.e(IllegalStateException("Currency titles size is not equal with values"), "CurrencyDialog()")
        return
    }
    val settingsCurrencies = currencyEntries.mapIndexed { index, value ->
        MainSettingsState.SettingsCurrency(
            title = currencyTitles[index],
            value = value,
        )
    }
    DebtsDialog(
        title = stringResource(id = R.string.preference_main_settings_currency),
        onDismissRequest = { onClick(selectedCurrencyValue) }
    ) {
        LazyColumn(
            modifier = modifier
        ) {
            settingsCurrencies
                .onEach {
                    item {
                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = selectedCurrencyValue == it.value,
                                    onClick = { onClick(it.value) },
                                    role = Role.RadioButton
                                )
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            RadioButton(
                                selected = selectedCurrencyValue == it.value,
                                onClick = { onClick(it.value) }
                            )
                            Text(
                                text = it.title,
                            )
                        }
                    }
                }
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun CurrencyAlertDialogPreview() {
    AppTheme {
        CurrencyDialog(
            selectedCurrencyValue = "€",
            onClick = {}
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CurrencyAlertDialogPreviewDark() {
    AppTheme {
        CurrencyDialog(
            selectedCurrencyValue = "€",
            onClick = {}
        )
    }
}
