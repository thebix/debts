package debts.feature.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import debts.core.common.android.extensions.toFormattedCurrency
import net.thebix.debts.feature.details.R
import kotlin.math.roundToInt
import net.thebix.debts.core.resource.R as ResourceR

internal val SCREEN_PADDING: Dp = 16.dp

/** Start of every text column on the screen: the icons and the avatar sit in the gutter before it. */
internal val CONTENT_START: Dp = 64.dp

private val TOOLBAR_HEIGHT: Dp = 64.dp
private val TOOLBAR_PADDING: Dp = 4.dp

/** The band under the toolbar that holds the avatar and the name while the header is expanded. */
private val NAME_BAND_HEIGHT: Dp = 74.dp
private val AVATAR_EXPANDED_SIZE: Dp = 40.dp
private val AVATAR_COLLAPSED_SIZE: Dp = 32.dp
private val ICON_BUTTON_SIZE: Dp = 48.dp
private val ICON_SIZE: Dp = 20.dp
private val NAME_COLLAPSED_GAP: Dp = 12.dp
private const val NAME_COLLAPSED_SCALE = 0.7f
private const val NAME_PIVOT_Y = 0.5f

/**
 * Header of the Details screen: a pinned toolbar, the debt block that scrolls away and the pinned
 * "History" title. While it collapses, the avatar and the name travel from the band under the
 * toolbar into the toolbar itself.
 *
 * @param fraction 0 is fully expanded, 1 is fully collapsed.
 */
@Composable
internal fun DetailsHeader(
    uiState: DetailsUiState,
    fraction: Float,
    actions: DetailsHeaderActions,
    onCollapsibleHeightChanged: (Float) -> Unit,
) {
    val nameBandHeightPx = with(LocalDensity.current) { NAME_BAND_HEIGHT.toPx() }
    Column {
        DetailsTopBar(uiState.name, uiState.avatarUrl, fraction, actions)
        Box(
            Modifier
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val visibleHeight = (placeable.height * (1f - fraction)).roundToInt()
                    layout(placeable.width, visibleHeight) {
                        placeable.place(0, visibleHeight - placeable.height)
                    }
                }
        ) {
            DebtBlock(
                uiState = uiState,
                actions = actions,
                modifier = Modifier.onSizeChanged { onCollapsibleHeightChanged(nameBandHeightPx + it.height) },
            )
        }
        HistoryTitle()
    }
}

internal data class DetailsHeaderActions(
    val onBackClicked: () -> Unit = {},
    val onShareClicked: () -> Unit = {},
    val onRemoveDebtorClicked: () -> Unit = {},
    val onChangeClicked: () -> Unit = {},
    val onClearClicked: () -> Unit = {},
)

@Composable
private fun DetailsTopBar(name: String, avatarUrl: String, fraction: Float, actions: DetailsHeaderActions) {
    // Same colours as the Home and Preferences app bars; the status bar icons are light, so the bar must stay dark.
    Surface(color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(TOOLBAR_HEIGHT + NAME_BAND_HEIGHT * (1f - fraction))
        ) {
            ToolbarButtons(actions)
            val avatarSize = lerp(AVATAR_EXPANDED_SIZE, AVATAR_COLLAPSED_SIZE, fraction)
            val collapsedAvatarStart = TOOLBAR_PADDING + ICON_BUTTON_SIZE + TOOLBAR_PADDING
            AsyncImage(
                model = avatarUrl.ifBlank { ResourceR.mipmap.ic_launcher },
                contentDescription = null,
                modifier = Modifier
                    .padding(
                        start = lerp(SCREEN_PADDING, collapsedAvatarStart, fraction),
                        top = lerp(TOOLBAR_HEIGHT + SCREEN_PADDING, (TOOLBAR_HEIGHT - AVATAR_COLLAPSED_SIZE) / 2, fraction),
                    )
                    .size(avatarSize)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            val expandedNameStart = SCREEN_PADDING + AVATAR_EXPANDED_SIZE + SCREEN_PADDING
            val collapsedNameStart = collapsedAvatarStart + AVATAR_COLLAPSED_SIZE + NAME_COLLAPSED_GAP
            // The name is scaled down as it collapses, so its layout width is wider than what ends up visible.
            val collapsedNameWidth =
                (maxWidth - collapsedNameStart - ICON_BUTTON_SIZE * 2 - TOOLBAR_PADDING * 2) / NAME_COLLAPSED_SCALE
            DebtorName(
                name = name,
                scale = lerp(1f, NAME_COLLAPSED_SCALE, fraction),
                modifier = Modifier
                    .padding(
                        start = lerp(expandedNameStart, collapsedNameStart, fraction),
                        top = lerp(TOOLBAR_HEIGHT, 0.dp, fraction),
                    )
                    .height(lerp(NAME_BAND_HEIGHT, TOOLBAR_HEIGHT, fraction))
                    .width(lerp(maxWidth - expandedNameStart - SCREEN_PADDING, collapsedNameWidth, fraction)),
            )
        }
    }
}

@Composable
private fun ToolbarButtons(actions: DetailsHeaderActions) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(TOOLBAR_HEIGHT).padding(horizontal = TOOLBAR_PADDING),
    ) {
        IconButton(onClick = actions.onBackClicked) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = actions.onRemoveDebtorClicked) {
            Icon(
                painterResource(ResourceR.drawable.ic_delete),
                contentDescription = stringResource(R.string.details_menu_delete),
            )
        }
        IconButton(onClick = actions.onShareClicked) {
            Icon(
                painterResource(ResourceR.drawable.ic_share),
                contentDescription = stringResource(ResourceR.string.details_menu_share),
            )
        }
    }
}

@Composable
private fun DebtorName(name: String, scale: Float, modifier: Modifier = Modifier) {
    val pivotX = if (LocalLayoutDirection.current == LayoutDirection.Rtl) 1f else 0f
    Box(contentAlignment = Alignment.CenterStart, modifier = modifier) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(pivotX, NAME_PIVOT_Y)
            },
        )
    }
}

@Composable
private fun DebtBlock(uiState: DetailsUiState, actions: DetailsHeaderActions, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(top = SCREEN_PADDING, end = SCREEN_PADDING, bottom = 20.dp)) {
        GutterIcon(ResourceR.drawable.ic_wallet)
        Column {
            Text(
                text = stringResource(R.string.details_debt_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(ResourceR.string.details_debt_total),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = SCREEN_PADDING),
            )
            Text(
                text = stringResource(
                    R.string.details_debt_amount,
                    uiState.currency,
                    uiState.amount.toFormattedCurrency(),
                ),
                style = MaterialTheme.typography.headlineMedium,
            )
            Row(Modifier.padding(top = 20.dp)) {
                OutlinedButton(onClick = actions.onChangeClicked) {
                    Text(stringResource(R.string.details_debt_change))
                }
                Spacer(Modifier.width(SCREEN_PADDING))
                Button(onClick = actions.onClearClicked, enabled = uiState.debts.isNotEmpty()) {
                    Text(stringResource(R.string.details_debt_clear))
                }
            }
        }
    }
}

@Composable
private fun HistoryTitle() {
    Column {
        HorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = SCREEN_PADDING),
        ) {
            GutterIcon(ResourceR.drawable.ic_clock)
            Text(
                text = stringResource(R.string.details_history_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GutterIcon(iconRes: Int) {
    Box(Modifier.width(CONTENT_START).padding(start = SCREEN_PADDING)) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(ICON_SIZE),
        )
    }
}
