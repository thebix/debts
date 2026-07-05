package debts.feature.preferences

data class PreferencesUiState(
    val selectedCurrency: String = "",
    val isCustomCurrency: Boolean = false,
    val customCurrencyText: String = "",
    val syncStatus: SyncStatus = SyncStatus.Idle,
    val versionName: String = "",
) {

    enum class SyncStatus { Idle, Updating, Updated, Error }

    companion object {
        val STANDARD_CURRENCIES = listOf("$", "€", "£", "₽", "¥", "Дин.", "Ft", "🍎")
        const val CUSTOM_KEY = "Custom"
    }
}
