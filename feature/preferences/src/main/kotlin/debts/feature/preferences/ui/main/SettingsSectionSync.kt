package debts.feature.preferences.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import debts.feature.preferences.mvi.MainSettingsState
import net.thebix.debts.feature.preferences.R

@Composable
internal fun SettingsSyncronizationSection(
    modifier: Modifier = Modifier,
    state: MainSettingsState,
    onSyncClick: () -> Unit,
) {
    SettingsSection(
        modifier = modifier,
    ) {
        SettingsSectionTitle(
            text = stringResource(id = R.string.preference_main_settings_sync_title),
        )
        SettingsSectionItem(
            modifier = modifier.clickable { onSyncClick() },
            title = stringResource(id = R.string.preference_main_settings_sync_contacts),
            subtitle = {
                val text = stringResource(
                    id = when (state.syncronisationState) {
                        MainSettingsState.UpdateState.START -> R.string.preference_main_settings_state_updating
                        MainSettingsState.UpdateState.END -> R.string.preference_main_settings_state_updated
                        MainSettingsState.UpdateState.ERROR -> R.string.preference_main_settings_state_error
                        MainSettingsState.UpdateState.IDLE -> R.string.preference_main_settings_sync_contacts_summary
                    }
                )
                SectionItemSubtitle(text = text)
            }
        )
    }
}
