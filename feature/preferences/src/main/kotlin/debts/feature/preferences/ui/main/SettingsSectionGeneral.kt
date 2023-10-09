package debts.feature.preferences.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import debts.feature.preferences.mvi.MainSettingsState
import net.thebix.debts.feature.preferences.R
import timber.log.Timber

@Composable
internal fun SettingsGeneralSection(
    modifier: Modifier = Modifier,
    state: MainSettingsState,
    onCurrencyClick: () -> Unit,
    onCustomCurrencyClick: () -> Unit,
) {
    val currencyTitles = stringArrayResource(id = R.array.preference_main_settings_entries)
    val currencyEntries = stringArrayResource(id = R.array.preference_main_settings_values)
    val currencyListSummaryText = if (currencyTitles.size != currencyEntries.size) {
        Timber.e(IllegalStateException("Currency titles size is not equal with values"), "SettingsGeneralSection()")
        currencyTitles.firstOrNull() ?: ""
    } else {
        val currencyListSelectedValueIndex = currencyEntries.indexOf(state.currencyListSelection)
        if (currencyListSelectedValueIndex == -1) {
            Timber.e(IllegalStateException("Can't find currency title by index"), "SettingsGeneralSection()")
            currencyTitles.first()
        } else {
            currencyTitles[currencyListSelectedValueIndex]
        }
    }
    SettingsSection(
        modifier = modifier
    ) {
        SettingsSectionTitle(
            text = stringResource(id = R.string.preference_main_settings_general_title)
        )
        SettingsSectionItem(
            modifier = modifier
                .clickable { onCurrencyClick() },
            title = stringResource(id = R.string.preference_main_settings_currency),
            subtitle = {
                SectionItemSubtitle(
                    text = stringResource(id = R.string.preference_main_settings_currency_summary, currencyListSummaryText)
                )
            },
        )
        if (state.currencyListSelection == "Custom") {
            SettingsSectionItem(
                modifier = modifier.clickable { onCustomCurrencyClick() },
                title = stringResource(id = R.string.preference_main_settings_currency_custom),
                subtitle = {
                    SectionItemSubtitle(
                        text = stringResource(id = R.string.preference_main_settings_currency_summary, state.currency)
                    )
                },
            )
        }
    }
    SettingsSectionItem(
        title = stringResource(id = R.string.preference_main_settings_version),
        subtitle = { SectionItemSubtitle(text = state.version) },
    )
    Divider()
}
