package debts.feature.details

sealed interface DetailsEvent {

    data class ShowAddDebtDialog(val name: String, val avatarUrl: String) : DetailsEvent

    data class ShowEditDebtDialog(
        val name: String,
        val avatarUrl: String,
        val debtId: Long,
        val amount: Double,
        val comment: String,
        val date: Long,
    ) : DetailsEvent

    data object DebtAdded : DetailsEvent
    data object DebtChanged : DetailsEvent
    data object EmptyDebtAmount : DetailsEvent
    data class ShareDebtor(val message: String) : DetailsEvent
    data object Close : DetailsEvent
}
