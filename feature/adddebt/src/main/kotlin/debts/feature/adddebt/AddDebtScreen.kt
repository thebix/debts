@file:OptIn(ExperimentalMaterial3Api::class)

package debts.feature.adddebt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import debts.core.common.android.extensions.toSimpleDateString
import debts.core.resource.theme.AppTheme
import debts.feature.contacts.adapter.ContactsItemViewModel
import net.thebix.debts.feature.adddebt.R
import java.util.Date

private val AVATAR_SIZE: Dp = 40.dp
private val AVATAR_GAP: Dp = 8.dp
private val MAX_SUGGESTIONS_HEIGHT: Dp = 220.dp
private val SUGGESTIONS_ELEVATION: Dp = 6.dp
private const val MIN_QUERY_LENGTH = 2

@Suppress("LongParameterList")
@Composable
fun AddDebtContent(
    uiState: AddDebtUiState,
    isEdit: Boolean,
    onNameChanged: (String) -> Unit,
    onContactSelected: (ContactsItemViewModel) -> Unit,
    onAmountChanged: (String) -> Unit,
    onSubtractChanged: (Boolean) -> Unit,
    onCommentChanged: (String) -> Unit,
    onCalendarClicked: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onDatePickerDismissed: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nameFocusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }
    InitialFocusEffect(
        nameFirst = uiState.name.isBlank() && uiState.canChangeDebtor,
        nameFocusRequester = nameFocusRequester,
        amountFocusRequester = amountFocusRequester,
    )
    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(if (isEdit) R.string.home_add_debt_change_title else R.string.home_add_debt_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(16.dp))
        AvatarNameRow(
            avatarUrl = uiState.avatarUrl,
            name = uiState.name,
            canChangeDebtor = uiState.canChangeDebtor,
            onNameChanged = onNameChanged,
            nameFocusRequester = nameFocusRequester,
        )
        if (uiState.canChangeDebtor && uiState.contactId == null) {
            NameSuggestions(
                name = uiState.name,
                contacts = uiState.contacts,
                onContactSelected = {
                    onContactSelected(it)
                    // As in the original dialog: once the contact is chosen, the amount is what comes next.
                    amountFocusRequester.requestFocus()
                },
            )
        }
        Spacer(Modifier.height(12.dp))
        DebtActionRow(isSubtract = uiState.isSubtract, onSubtractChanged = onSubtractChanged)
        Spacer(Modifier.height(8.dp))
        AmountField(
            value = uiState.amountText,
            isError = uiState.amountError,
            onValueChange = onAmountChanged,
            focusRequester = amountFocusRequester,
        )
        CalendarRow(dateMs = uiState.dateMs, onClick = onCalendarClicked)
        CommentField(value = uiState.comment, onValueChange = onCommentChanged)
        Spacer(Modifier.height(16.dp))
        DebtDialogButtons(onDismiss = onDismiss, onConfirm = onConfirm)
    }
    if (uiState.showDatePicker) {
        AddDebtDatePickerDialog(
            dateMs = uiState.dateMs,
            onDateSelected = onDateSelected,
            onDismiss = onDatePickerDismissed,
        )
    }
}

@Composable
private fun InitialFocusEffect(
    nameFirst: Boolean,
    nameFocusRequester: FocusRequester,
    amountFocusRequester: FocusRequester,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    // Same as the original dialog: the name when there is nothing to pick yet, the amount otherwise.
    LaunchedEffect(Unit) {
        withFrameNanos { }
        (if (nameFirst) nameFocusRequester else amountFocusRequester).requestFocus()
        keyboardController?.show()
    }
}

@Composable
private fun NameSuggestions(
    name: String,
    contacts: List<ContactsItemViewModel>,
    onContactSelected: (ContactsItemViewModel) -> Unit,
) {
    val suggestions = remember(name, contacts) {
        if (name.length < MIN_QUERY_LENGTH) {
            emptyList()
        } else {
            contacts.filter { it.name.contains(name, ignoreCase = true) }
        }
    }
    if (suggestions.isEmpty()) return
    ContactSuggestions(
        contacts = suggestions,
        onContactSelected = onContactSelected,
        modifier = Modifier
            // Zero layout height and a higher z-index: the list floats over the fields below instead of pushing them.
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(maxHeight = Constraints.Infinity))
                layout(placeable.width, 0) { placeable.place(0, 0) }
            }
            .zIndex(1f)
            .padding(start = AVATAR_SIZE + AVATAR_GAP, top = 4.dp),
    )
}

@Composable
private fun AmountField(
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.home_add_debt_amount)) },
        isError = isError,
        supportingText = if (isError) {
            { Text(stringResource(R.string.home_add_debt_amount_error)) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
    )
}

@Composable
private fun CommentField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.home_add_debt_comment)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AddDebtDatePickerDialog(
    dateMs: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMs)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let(onDateSelected) ?: onDismiss()
            }) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun AvatarNameRow(
    avatarUrl: String,
    name: String,
    canChangeDebtor: Boolean,
    onNameChanged: (String) -> Unit,
    nameFocusRequester: FocusRequester,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = avatarUrl.ifBlank { net.thebix.debts.core.resource.R.mipmap.ic_launcher },
            contentDescription = null,
            modifier = Modifier
                .size(AVATAR_SIZE)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(AVATAR_GAP))
        ContactNameField(
            name = name,
            canChangeDebtor = canChangeDebtor,
            onNameChanged = onNameChanged,
            focusRequester = nameFocusRequester,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ContactNameField(
    name: String,
    canChangeDebtor: Boolean,
    onNameChanged: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    // The caret goes to the end whenever the name is replaced from outside, e.g. when a suggestion is picked.
    var fieldValue by remember { mutableStateOf(TextFieldValue(name, TextRange(name.length))) }
    if (fieldValue.text != name) {
        fieldValue = TextFieldValue(name, TextRange(name.length))
    }
    OutlinedTextField(
        value = fieldValue,
        onValueChange = {
            fieldValue = it
            if (it.text != name) onNameChanged(it.text)
        },
        enabled = canChangeDebtor,
        label = { Text(stringResource(R.string.home_add_debt_name)) },
        singleLine = true,
        modifier = modifier.focusRequester(focusRequester),
    )
}

@Composable
private fun ContactSuggestions(
    contacts: List<ContactsItemViewModel>,
    onContactSelected: (ContactsItemViewModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        // Lifted look: container surface, M3 elevation level 3 and a hairline outline so it stays distinct on a white dialog.
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = SUGGESTIONS_ELEVATION,
        shadowElevation = SUGGESTIONS_ELEVATION,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = MAX_SUGGESTIONS_HEIGHT)
                .verticalScroll(rememberScrollState()),
        ) {
            contacts.forEach { contact ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContactSelected(contact) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    AsyncImage(
                        model = contact.avatarUrl.ifBlank { net.thebix.debts.core.resource.R.mipmap.ic_launcher },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(text = contact.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun DebtActionRow(isSubtract: Boolean, onSubtractChanged: (Boolean) -> Unit) {
    Column {
        Text(
            text = stringResource(R.string.home_add_debt_action),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = !isSubtract, onClick = { onSubtractChanged(false) })
            Text(
                text = stringResource(R.string.home_add_debt_add),
                modifier = Modifier.clickable { onSubtractChanged(false) },
            )
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = isSubtract, onClick = { onSubtractChanged(true) })
            Text(
                text = stringResource(R.string.home_add_debt_subtract),
                modifier = Modifier.clickable { onSubtractChanged(true) },
            )
        }
    }
}

@Composable
private fun DebtDialogButtons(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onConfirm) { Text(stringResource(R.string.home_add_debt_confirm)) }
    }
}

@Composable
private fun CalendarRow(dateMs: Long, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(net.thebix.debts.core.resource.R.drawable.ic_calendar),
            contentDescription = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(Date(dateMs).toSimpleDateString())
    }
}

@Preview(showBackground = true)
@Composable
private fun AddDebtContentPreview() {
    AppTheme {
        AddDebtContent(
            uiState = AddDebtUiState(dateMs = 1_700_000_000_000L),
            isEdit = false,
            onNameChanged = {},
            onContactSelected = {},
            onAmountChanged = {},
            onSubtractChanged = {},
            onCommentChanged = {},
            onCalendarClicked = {},
            onDateSelected = {},
            onDatePickerDismissed = {},
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddDebtContentEditPreview() {
    AppTheme {
        AddDebtContent(
            uiState = AddDebtUiState(
                name = "Alice",
                amountText = "42.5",
                comment = "for coffee",
                dateMs = 1_700_000_000_000L,
                canChangeDebtor = false,
                existingDebtId = 1L,
                contacts = listOf(
                    ContactsItemViewModel(id = 1L, name = "Alice", avatarUrl = ""),
                ),
            ),
            isEdit = true,
            onNameChanged = {},
            onContactSelected = {},
            onAmountChanged = {},
            onSubtractChanged = {},
            onCommentChanged = {},
            onCalendarClicked = {},
            onDateSelected = {},
            onDatePickerDismissed = {},
            onConfirm = {},
            onDismiss = {},
        )
    }
}
