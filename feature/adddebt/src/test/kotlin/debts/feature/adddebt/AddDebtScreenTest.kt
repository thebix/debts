package debts.feature.adddebt

import com.github.takahirom.roborazzi.captureRoboImage
import debts.core.resource.theme.AppTheme
import debts.feature.contacts.adapter.ContactsItemViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val SCREENSHOTS = "src/test/screenshots"
private const val CLASS = "debts.feature.adddebt.AddDebtScreenTest"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-normal-notlong-notround-port-notnight-mdpi-finger")
class AddDebtScreenTest {

    @Test
    fun addDebtScreen_empty() {
        captureRoboImage("$SCREENSHOTS/$CLASS.addDebtScreen_empty.png") {
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
    }

    @Test
    fun addDebtScreen_filledEdit() {
        captureRoboImage("$SCREENSHOTS/$CLASS.addDebtScreen_filledEdit.png") {
            AppTheme {
                AddDebtContent(
                    uiState = AddDebtUiState(
                        name = "Alice",
                        amountText = "42.5",
                        isSubtract = false,
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
    }

    @Test
    fun addDebtScreen_amountError() {
        captureRoboImage("$SCREENSHOTS/$CLASS.addDebtScreen_amountError.png") {
            AppTheme {
                AddDebtContent(
                    uiState = AddDebtUiState(
                        amountText = "1".repeat(17),
                        amountError = true,
                        dateMs = 1_700_000_000_000L,
                    ),
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
    }

    @Test
    fun addDebtScreen_suggestions() {
        captureRoboImage("$SCREENSHOTS/$CLASS.addDebtScreen_suggestions.png") {
            AppTheme {
                AddDebtContent(
                    uiState = AddDebtUiState(
                        name = "pa",
                        dateMs = 1_700_000_000_000L,
                        contacts = listOf(
                            ContactsItemViewModel(id = 1L, name = "Pavel", avatarUrl = ""),
                            ContactsItemViewModel(id = 2L, name = "Papa", avatarUrl = ""),
                            ContactsItemViewModel(id = 3L, name = "Alice", avatarUrl = ""),
                        ),
                    ),
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
    }
}
