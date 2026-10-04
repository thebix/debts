package debts.feature.adddebt

import debts.core.common.android.mvvm.BaseViewModel
import debts.feature.contacts.adapter.ContactsItemViewModel
import kotlin.math.absoluteValue

class AddDebtViewModel : BaseViewModel<AddDebtUiState, AddDebtEvent>(AddDebtUiState()) {

    companion object {
        private const val AMOUNT_MAX_LENGTH = 16
    }

    @Suppress("LongParameterList")
    fun init(
        name: String,
        avatarUrl: String,
        amount: Double,
        comment: String,
        dateMs: Long,
        contacts: List<ContactsItemViewModel>,
        existingDebtId: Long?,
        canChangeDebtor: Boolean,
    ) {
        updateState {
            copy(
                name = name,
                avatarUrl = avatarUrl,
                amountText = if (amount != 0.0) amount.absoluteValue.toString() else "",
                isSubtract = amount < 0,
                comment = comment,
                dateMs = dateMs,
                contacts = contacts,
                existingDebtId = existingDebtId,
                canChangeDebtor = canChangeDebtor,
            )
        }
    }

    fun onNameChanged(value: String) {
        updateState {
            val hadContact = contactId != null
            copy(
                name = value,
                contactId = null,
                avatarUrl = if (hadContact) "" else avatarUrl,
            )
        }
    }

    fun onContactSelected(contact: ContactsItemViewModel) {
        updateState {
            copy(
                contactId = contact.id,
                name = contact.name,
                avatarUrl = contact.avatarUrl,
            )
        }
    }

    fun onAmountChanged(value: String) {
        val amount = sanitizeAmount(value)
        updateState {
            copy(
                amountText = amount,
                amountError = amount.length > AMOUNT_MAX_LENGTH,
            )
        }
    }

    fun onSubtractChanged(isSubtract: Boolean) {
        updateState { copy(isSubtract = isSubtract) }
    }

    fun onCommentChanged(value: String) {
        updateState { copy(comment = value) }
    }

    fun onCalendarClicked() {
        updateState { copy(showDatePicker = true) }
    }

    fun onDateSelected(ms: Long) {
        updateState { copy(dateMs = ms, showDatePicker = false) }
    }

    fun onDatePickerDismissed() {
        updateState { copy(showDatePicker = false) }
    }

    fun onConfirm() {
        sendEvent(AddDebtEvent.Confirmed(buildResult()))
    }

    // The sign comes from the Lent/Borrowed switch, so the field takes digits and one decimal separator only,
    // like the numberDecimal EditText of the original dialog.
    private fun sanitizeAmount(raw: String): String {
        var hasSeparator = false
        return buildString {
            raw.forEach { char ->
                when {
                    char in '0'..'9' -> append(char)
                    (char == '.' || char == ',') && !hasSeparator -> {
                        hasSeparator = true
                        append('.')
                    }
                }
            }
        }
    }

    private fun buildResult(): DebtLayoutData {
        val s = uiState.value
        val amount = runCatching {
            if (s.amountText.length > AMOUNT_MAX_LENGTH) {
                0.0
            } else {
                s.amountText.toDouble() * if (s.isSubtract) -1 else 1
            }
        }.getOrDefault(0.0)
        return DebtLayoutData(
            contactId = s.contactId,
            name = s.name.trim(),
            amount = amount,
            comment = s.comment.trim(),
            existingDebtId = s.existingDebtId,
            date = s.dateMs,
        )
    }
}
