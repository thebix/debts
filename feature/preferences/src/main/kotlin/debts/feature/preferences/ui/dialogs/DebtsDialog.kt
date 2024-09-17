@file: Suppress("FunctionNaming")

package debts.feature.preferences.ui.dialogs

import android.content.res.Configuration
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import debts.core.resource.theme.AppTheme

@Suppress("LongMethod")
@Composable
fun DebtsDialog(
    modifier: Modifier = Modifier,
    title: String,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
        ) {
            Column(
                modifier = Modifier
                    .scrollable(rememberScrollState(), orientation = Orientation.Vertical)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 8.dp),
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )
                content()
            }
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun CurrencyAlertDialogPreview() {
    CurrencyAlertDialogPreviewGeneric()
}

@Suppress("UnusedPrivateMember")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CurrencyAlertDialogPreviewDark() {
    CurrencyAlertDialogPreviewGeneric()
}

@Suppress("UnusedPrivateMember")
@Composable
private fun CurrencyAlertDialogPreviewGeneric() {
    AppTheme {
        DebtsDialog(
            title = "Test title",
            onDismissRequest = {}
        ) {
            Text(text = "Test")
        }
    }
}
