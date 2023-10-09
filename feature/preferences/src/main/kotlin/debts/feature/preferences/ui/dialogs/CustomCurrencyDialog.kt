@file: Suppress("FunctionNaming")

package debts.feature.preferences.ui.dialogs

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import debts.core.resource.theme.AppTheme
import net.thebix.debts.feature.preferences.R

@Composable
internal fun CustomCurrencyDialog(
    selectedCurrencyValue: String,
    onClick: (value: String) -> Unit,
) {
    var value by remember {
        mutableStateOf(
            TextFieldValue(
                text = selectedCurrencyValue,
                selection = TextRange(selectedCurrencyValue.length)
            )
        )
    }
    val focusRequester = FocusRequester()
    DebtsDialog(
        title = stringResource(id = R.string.preference_main_settings_currency_custom),
        onDismissRequest = { onClick(selectedCurrencyValue) }
    ) {
        // initialize focus reference to be able to request focus programmatically
        Column {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = value,
                onValueChange = { value = it },
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd

            ) {
                TextButton(
                    onClick = { onClick(value.text) },
                ) {
                    Text(stringResource(id = net.thebix.debts.core.common.R.string.default_positive_button))
                }
            }
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun CustomCurrencyDialogPreview() {
    CustomCurrencyDialogPreviewGeneric()
}

@Suppress("UnusedPrivateMember")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CustomCurrencyDialogPreviewDark() {
    CustomCurrencyDialogPreviewGeneric()
}

@Composable
private fun CustomCurrencyDialogPreviewGeneric() {
    AppTheme {
        CustomCurrencyDialog(
            selectedCurrencyValue = "€",
            onClick = {}
        )
    }
}
