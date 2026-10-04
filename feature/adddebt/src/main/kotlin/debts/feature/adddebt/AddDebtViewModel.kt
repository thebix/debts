package debts.feature.adddebt

import androidx.lifecycle.ViewModel
import debts.feature.contacts.adapter.ContactsItemViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.absoluteValue

class AddDebtViewModel : ViewModel() {

    companion object {
        private const val AMOUNT_MAX_LENGTH = 16
    }

    private val _uiState = MutableStateFlow(AddDebtUiState())
    val uiState: StateFlow<AddDebtUiState> = _uiState.asStateFlow()

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
        _uiState.update {
            it.copy(
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
        _uiState.update { state ->
            val hadContact = state.contactId != null
            state.copy(
                name = value,
                contactId = null,
                avatarUrl = if (hadContact) "" else state.avatarUrl,
            )
        }
    }

    fun onContactSelected(contact: ContactsItemViewModel) {
        _uiState.update {
            it.copy(
                contactId = contact.id,
                name = contact.name,
                avatarUrl = contact.avatarUrl,
            )
        }
    }

    fun onAmountChanged(value: String) {
        _uiState.update {
            it.copy(
                amountText = value,
                amountError = value.length > AMOUNT_MAX_LENGTH,
            )
        }
    }

    fun onSubtractChanged(isSubtract: Boolean) {
        _uiState.update { it.copy(isSubtract = isSubtract) }
    }

    fun onCommentChanged(value: String) {
        _uiState.update { it.copy(comment = value) }
    }

    fun onCalendarClicked() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onDateSelected(ms: Long) {
        _uiState.update { it.copy(dateMs = ms, showDatePicker = false) }
    }

    fun onDatePickerDismissed() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun buildResult(): DebtLayoutData {
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
