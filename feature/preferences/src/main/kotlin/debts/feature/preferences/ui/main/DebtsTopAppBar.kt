package debts.feature.preferences.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.thebix.debts.feature.preferences.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DebtsTopAppBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = @Composable {
            Text(
                text = stringResource(id = R.string.settings_title),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        navigationIcon = @Composable {
            Icon(
                modifier = Modifier
                    .padding(12.dp)
                    .clickable { onBackClick() },
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(id = R.string.preference_main_content_descr_go_back),
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    )
}
