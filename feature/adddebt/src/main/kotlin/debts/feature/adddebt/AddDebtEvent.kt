package debts.feature.adddebt

sealed interface AddDebtEvent {

    data class Confirmed(val result: DebtLayoutData) : AddDebtEvent
}
