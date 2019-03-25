package debts.common.android.prefs

import android.content.Context

interface Preferences {

    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)

    operator fun contains(key: String): Boolean
    fun remove(key: String)
    fun clear()
}


class AndroidPreferences(
    context: Context,
    preferencesName: String
) : Preferences {

    private val prefs = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    override fun getBoolean(key: String, defValue: Boolean): Boolean = prefs.getBoolean(key, defValue)
    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    override fun contains(key: String): Boolean = prefs.contains(key)

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }
}
