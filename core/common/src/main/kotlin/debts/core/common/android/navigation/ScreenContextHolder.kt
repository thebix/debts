package debts.core.common.android.navigation

interface ScreenContextHolder {

    companion object {

        const val ACTIVITY_HOME = "ACTIVITY_HOME"
        const val FRAGMENT_DEBTORS = "FRAGMENT_DEBTORS"
        const val FRAGMENT_DETAILS = "FRAGMENT_DETAILS"
        const val FRAGMENT_MAIN_PREFERENCES = "FRAGMENT_MAIN_PREFERENCES"
    }

    fun set(screenKey: String, contextHolder: ScreenContext)
    fun get(screenKey: String): ScreenContext?
    fun remove(screenKey: String)
}
