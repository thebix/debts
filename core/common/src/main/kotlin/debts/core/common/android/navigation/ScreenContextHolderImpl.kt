package debts.core.common.android.navigation

class ScreenContextHolderImpl : ScreenContextHolder {

    private val screens: MutableMap<String, ScreenContext> = mutableMapOf()

    override fun set(screenKey: String, contextHolder: ScreenContext) {
        screens[screenKey] = contextHolder
    }

    override fun get(screenKey: String): ScreenContext? = screens[screenKey]

    override fun remove(screenKey: String) {
        screens[screenKey]?.let { screen ->
            screen.dispose()
        }
        screens.remove(screenKey)
    }
}
