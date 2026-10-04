package debts.feature.details

/**
 * Callbacks of the Details screen. The defaults let previews and screenshot tests set only what they use.
 */
data class DetailsActions(
    val onBackClicked: () -> Unit = {},
    val onShareClicked: () -> Unit = {},
    val onRemoveDebtorConfirmed: () -> Unit = {},
    val onChangeClicked: () -> Unit = {},
    val onClearHistoryConfirmed: () -> Unit = {},
    val onDebtClicked: (Long) -> Unit = {},
    val onRemoveDebtConfirmed: (Long) -> Unit = {},
)
