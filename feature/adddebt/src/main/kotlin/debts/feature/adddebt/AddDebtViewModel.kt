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
        updateState {
            copy(
                amountText = value,
                amountError = value.length > AMOUNT_MAX_LENGTH,
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
