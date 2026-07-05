package debts.feature.preferences

sealed class PreferencesEvent {
    data class RequestPermission(val permission: String, val requestCode: Int) : PreferencesEvent()
    object ShowError : PreferencesEvent()
}
