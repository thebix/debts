package debts.feature.preferences

import androidx.compose.material3.SnackbarHostState
import com.github.takahirom.roborazzi.captureRoboImage
import debts.core.resource.theme.AppTheme
import debts.feature.preferences.PreferencesUiState.SyncStatus
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val SCREENSHOTS = "src/test/screenshots"
private const val CLASS = "debts.feature.preferences.PreferencesScreenTest"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-normal-notlong-notround-port-notnight-mdpi-finger")
class PreferencesScreenTest {

    @Test
    fun preferencesScreen_standardCurrency() {
        captureRoboImage("$SCREENSHOTS/$CLASS.preferencesScreen_standardCurrency.png") {
            AppTheme {
                PreferencesScreen(
                    uiState = PreferencesUiState(selectedCurrency = "$", versionName = "1.0.0 (42)"),
                    snackbarHostState = SnackbarHostState(),
                    onCurrencySelected = {},
                    onCustomCurrencyChanged = {},
                    onCustomCurrencySubmitted = {},
                    onSyncWithContactsClicked = {},
                    onNavigateUp = {},
                )
            }
        }
    }

    @Test
    fun preferencesScreen_customCurrency() {
        captureRoboImage("$SCREENSHOTS/$CLASS.preferencesScreen_customCurrency.png") {
            AppTheme {
                PreferencesScreen(
                    uiState = PreferencesUiState(
                        selectedCurrency = PreferencesUiState.CUSTOM_KEY,
                        isCustomCurrency = true,
                        customCurrencyText = "custom value",
                        versionName = "1.0.0 (42)",
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onCurrencySelected = {},
                    onCustomCurrencyChanged = {},
                    onCustomCurrencySubmitted = {},
                    onSyncWithContactsClicked = {},
                    onNavigateUp = {},
                )
            }
        }
    }

    @Test
    fun preferencesScreen_syncUpdating() {
        captureRoboImage("$SCREENSHOTS/$CLASS.preferencesScreen_syncUpdating.png") {
            AppTheme {
                PreferencesScreen(
                    uiState = PreferencesUiState(
                        syncStatus = SyncStatus.Updating,
                        selectedCurrency = "$",
                        versionName = "1.0.0 (42)",
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onCurrencySelected = {},
                    onCustomCurrencyChanged = {},
                    onCustomCurrencySubmitted = {},
                    onSyncWithContactsClicked = {},
                    onNavigateUp = {},
                )
            }
        }
    }
}
